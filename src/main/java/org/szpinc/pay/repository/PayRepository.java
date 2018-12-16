package org.szpinc.pay.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.szpinc.pay.bean.Pay;

import java.util.List;

public interface PayRepository extends JpaRepository<Pay,String> {

    List<Pay> getByStatusIs (int status);

    List<Pay> getByStatusIsNotAndStatusIsNot (int status1, int status2);

}
