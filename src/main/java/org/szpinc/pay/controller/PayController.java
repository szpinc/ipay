package org.szpinc.pay.controller;


import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.support.HttpRequestHandlerServlet;
import org.springframework.web.server.handler.ExceptionHandlingWebHandler;
import org.szpinc.pay.bean.Pay;
import org.szpinc.pay.bean.dto.DataTableResult;
import org.szpinc.pay.bean.dto.Result;
import org.szpinc.pay.common.utils.*;
import org.szpinc.pay.service.PayService;

import javax.jws.WebParam;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@Api(tags = "开放接口", description = "支付管理")
public class PayController {

    private final Logger LOG = LoggerFactory.getLogger(PayController.class);

    @Autowired
    private PayService payService;
    @Autowired
    private RedisUtils redisUtils;
    @Autowired
    private ResultUtils<Pay> resultUtils;
    @Autowired
    private StringUtils stringUtils;
    @Autowired
    private EmailUtil emailUtil;
    @Autowired
    private IpUtils ipUtils;
    @Value("${ip.expire}")
    private Long IP_EXPIRE;
    @Value("${my.token}")
    private String MY_TOKEN;
    @Value("${mail.sender}")
    private String MAIL_SENDER;
    @Value("${mail.receiver}")
    private String MAIL_RECEIVER;
    @Value("${token.admin.expire}")
    private long ADMIN_EXPIRE;
    @Value("${token.fake.expire}")
    private long FAKE_EXPIRE;
    @Value("${fake.pre}")
    private String FAKE_PRE;
    @Value("${server.url}")
    private String SERVER_URL;


    @GetMapping("/pay/list")
    @ApiOperation(value = "获取未支付数据")
    public DataTableResult getPayList() {
        DataTableResult tableResult = new DataTableResult();
        List<Pay> payList = new ArrayList<>();
        try {
            payList = payService.getNotPayList();
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("获取未支付数据失败", e);
            }
            tableResult.setSuccess(false);
            tableResult.setError("获取未支付数据失败");
        }
        tableResult.setData(payList);
        tableResult.setSuccess(true);
        return tableResult;
    }

    @GetMapping("/pay/check/list")
    @ApiOperation("获取支付审核列表")
    public DataTableResult getCheckList() {
        DataTableResult tableResult = new DataTableResult();
        List<Pay> payList = new ArrayList<>();
        try {
            payList = payService.getPayList(0);
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("获取未支付审核列表失败", e);
            }
            tableResult.setSuccess(false);
            tableResult.setError("获取未支付审核列表失败");
        }
        tableResult.setData(payList);
        tableResult.setSuccess(true);
        return tableResult;
    }

    @GetMapping("/pay/{id}")
    @ApiOperation("获取支付数据")
    public Result<Object> getPayList(@PathVariable("id") String id, @RequestParam(required = true) String token) {
        String temp = redisUtils.get(token);

        if (!token.equals(temp)) {
            return new ResultUtils<Object>().setData("无效的token");
        }

        Pay pay = null;
        try {
            pay = payService.getPay(getPayId(id));
        } catch (Exception e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("获取支付数据失败", e);
            }
            return new ResultUtils<Object>().setErrorMsg("获取支付数据失败");
        }

        return new ResultUtils<Object>().setData(pay);
    }

    @PostMapping("/pay/add")
    @ApiOperation("添加支付订单")
    public Result<Object> addPay(@ModelAttribute Pay pay, HttpServletRequest request) {
        if (StringUtils.isBlank(pay.getUserName()) || StringUtils.isBlank(String.valueOf(pay.getMoney())) || StringUtils.isBlank(pay.getEmail()) || EmailUtil.checkEmail(pay.getEmail())) {
            return new ResultUtils<Object>().setErrorMsg("请填写完整信息和正确的通知邮箱");
        }
        String ip = IpUtils.getIpAddr(request);
        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            ip = "127.0.0.1";
        }
        String temp = redisUtils.get(ip);
        if (StringUtils.isNotBlank(temp)) {
            return new ResultUtils<Object>().setErrorMsg("您提交的太频繁啦，请2分钟后再试");
        }
        try {
            payService.addPay(pay);
            pay.setTime(StringUtils.getTimeStamp(new Date()));
        } catch (Exception e) {
            return new ResultUtils<Object>().setErrorMsg("添加捐赠支付订单失败");
        }

        //记录缓存
        redisUtils.set(ip, "added", IP_EXPIRE, TimeUnit.MINUTES);
        //给管理员发送邮件
        String tokenAdmin = UUID.randomUUID().toString();
        redisUtils.set(pay.getId(), tokenAdmin, ADMIN_EXPIRE, TimeUnit.DAYS);
        pay = getAdminUrl(pay, pay.getId(), tokenAdmin, MY_TOKEN);
        emailUtil.sendTemplateMail(MAIL_SENDER, MAIL_RECEIVER, "【iPay个人收款系统】待审核处理", "email-admin", pay);

        //给假管理员发送审核邮件
        if (StringUtils.isNotBlank(pay.getTestEmail()) && emailUtil.checkEmail(pay.getTestEmail())) {
            Pay pay2 = payService.getPay(pay.getId());
            String tokenFake = UUID.randomUUID().toString();
            redisUtils.set(FAKE_PRE + pay.getId(), tokenFake, FAKE_EXPIRE, TimeUnit.HOURS);
            pay2 = getAdminUrl(pay2, FAKE_PRE + pay.getId(), tokenFake, MY_TOKEN);
            emailUtil.sendTemplateMail(MAIL_SENDER, pay.getTestEmail(), "【iPay个人收款支付系统】待审核处理", "email-fake", pay2);
        }
        return new ResultUtils<Object>().setData(null);


    }


    @PostMapping("/pay/edit")
    @ApiOperation("编辑支付订单")
    public Result<Object> editPay(@ModelAttribute Pay pay, @RequestParam String id, @RequestParam String token) {
        String temp = redisUtils.get(id);
        if (!token.equals(temp)) {
            return new ResultUtils<Object>().setErrorMsg("无效的token或链接");
        }

        try {
            pay.setId(getPayId(pay.getId()));
            Pay p = payService.getPay(getPayId(pay.getId()));
            pay.setStatus(p.getStatus());
        } catch (Exception e) {
            return new ResultUtils<Object>().setErrorMsg("编辑支付订单失败");
        }

        if (id.contains(FAKE_PRE)) {
            redisUtils.set(id, "", 1L, TimeUnit.SECONDS);
        }

        return new ResultUtils<Object>().setData(null);
    }

    @GetMapping("/pay/pass")
    @ApiOperation("审核通过支付订单")
    public String addPay(@RequestParam String id, @RequestParam String token, @RequestParam String myToken, Model model) {
        String temp = redisUtils.get(id);
        if (!token.equals(temp)) {
            model.addAttribute("errorMsg", "无效的token或链接");
            return "/500";
        }

        if (!myToken.equals(MY_TOKEN)) {
            model.addAttribute("errorMsg", "您未通过二次验证");
            return "/500";
        }

        try {
            payService.changePayStatus(getPayId(id), 1);
            //通知回调
            Pay pay = payService.getPay(getPayId(id));
            if (StringUtils.isNotBlank(pay.getEmail()) && EmailUtil.checkEmail(pay.getEmail())) {
                emailUtil.sendTemplateMail(MAIL_SENDER, pay.getEmail(), "【iPay个人收款支付系统】支付成功通知", "pay-success", pay);
            }
        } catch (Exception e) {
            model.addAttribute("errorMsg", "处理数据错误");
            return "/500";
        }
        return "redirect:/success";
    }

    @GetMapping("/pay/passNotShow")
    @ApiOperation("审核通过但不显示加入捐赠表")
    public String passNotShowPay(@RequestParam String id, @RequestParam String token, @RequestParam Model model) {
        String temp = redisUtils.get(id);
        if (!token.equals(temp)) {
            model.addAttribute("errorMsg", "无效的token或链接");
            return "/500";
        }


        try {
            payService.changePayStatus(getPayId(id), 3);
            Pay pay = payService.getPay(getPayId(id));
            if (StringUtils.isNotBlank(pay.getEmail()) && EmailUtil.checkEmail(pay.getEmail())) {
                emailUtil.sendTemplateMail(MAIL_SENDER, pay.getEmail(), "【iPay个人收款支付系统】支付成功通知", "pay-success", pay);
            }
        } catch (Exception e) {
            model.addAttribute("errorMsg", "处理数据出错");
            return "/500";
        }
        if (id.contains(FAKE_PRE)) {
            redisUtils.set(id, "", 1L, TimeUnit.SECONDS);
        }
        return "redirect:/success";
    }


    @GetMapping("/pay/back")
    @ApiOperation("审核驳回支付订单")
    public String backPay(@RequestParam String id, @RequestParam String token, @RequestParam String myToken, @RequestParam Model model) {
        String temp = redisUtils.get(id);
        if (!token.equals(temp)) {
            model.addAttribute("errorMsg", "无效的token或链接");
            return "/500";
        }

        if (!myToken.equals(MY_TOKEN)) {
            model.addAttribute("errorMsg", "您未通过二次验证");
            return "/500";
        }

        try {
            payService.changePayStatus(getPayId(id), 2);
            //通知回调
            Pay pay = payService.getPay(getPayId(id));
            if (StringUtils.isNotBlank(pay.getEmail()) && EmailUtil.checkEmail(pay.getEmail())) {
                emailUtil.sendTemplateMail(MAIL_SENDER, pay.getEmail(), "【iPay个人收款支付系统】支付失败通知", "pay-fail", pay);
            }
        } catch (Exception e) {
            model.addAttribute("errorMsg", "处理数据出错");
            return "/500";
        }
        if (id.contains(FAKE_PRE)) {
            redisUtils.set(id, "", 1L, TimeUnit.SECONDS);
        }
        return "redirect:/success";
    }

    @GetMapping("/pay/del")
    @ApiOperation("删除支付订单")
    public Result<Object> delPay(@RequestParam String id, @RequestParam String token) {
        String temp = redisUtils.get(getPayId(id));
        if (!token.equals(temp)) {
            return new ResultUtils<Object>().setErrorMsg("无效的token或链接");
        }

        try {
            //通知回调
            Pay pay = payService.getPay(getPayId(id));
            if (StringUtils.isNotBlank(pay.getEmail()) && EmailUtil.checkEmail(pay.getEmail())) {
                emailUtil.sendTemplateMail(MAIL_SENDER, pay.getEmail(), "【iPay个人收款支付系统】支付失败通知", "pay-fail", pay);
            }
            payService.delPay(getPayId(id));
        } catch (Exception e) {
            LOG.error(e.getMessage());
            return new ResultUtils<Object>().setErrorMsg("删除支付订单失败");
        }
        if (id.contains(FAKE_PRE)) {
            redisUtils.set(id, "", 1L, TimeUnit.SECONDS);
        }
        return new ResultUtils<Object>().setData(null);
    }


    /**
     * 拼接管理员链接
     */
    public Pay getAdminUrl(Pay pay, String id, String token, String myToken) {

        String pass = SERVER_URL + "/pay/pass?id=" + id + "&token=" + token + "&myToken=" + myToken;
        pay.setPassUrl(pass);

        String back = SERVER_URL + "/pay/back?id=" + id + "&token=" + token + "&myToken=" + myToken;
        pay.setBackUrl(back);

        String passNotShow = SERVER_URL + "/pay/passNotShow?id=" + id + "&token=" + token;
        pay.setPassNotShowUrl(passNotShow);

        String edit = SERVER_URL + "/pay-edit?id=" + id + "&token=" + token;
        pay.setEditUrl(edit);

        String del = SERVER_URL + "/pay-del?id=" + id + "&token=" + token;
        pay.setDelUrl(del);
        return pay;
    }


    /**
     * 获得假管理ID
     *
     * @param id
     * @return
     */
    public String getPayId(String id) {
        if (id.contains(FAKE_PRE)) {
            String realId = id.substring(id.indexOf("-", 0) + 1, id.length());
            return realId;
        }
        return id;
    }

}
