package hello.springmvc.apply;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.event.EventListener;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.annotation.PostConstruct;

@SpringBootTest
public class InitTxTest {

    @Autowired Hello hello;

    @Test
    void go(){
        //@postContstruct가 스프링 컨테이너가빈에 등록되고 나서 바로 자동으로 initV1()를 실행해준다.
        //hello.initV1();
        //@postConstructer를 사용하지 않고 직접 호출하면 트랜잭션이 적용된다.
        //그런데 직접호출이 아니고 @PostConstructer에 의해 실행될떄는 트랜잭션이 적용되지 않는다.
    }

    @TestConfiguration
    static class InitTxTextConfig{
        @Bean
        Hello hello(){
            return new Hello();
        }
    }

    @Slf4j
    static class Hello{

        @PostConstruct
        @Transactional
        public void initV1(){
            boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello init @PostConstruct tx active={}" , isActive);
        }

        @EventListener(ApplicationReadyEvent.class)
        //스프링 컨테이너가 다떳을때 실행 한다는 것
        @Transactional
        public void initV2(){
            boolean isActive = TransactionSynchronizationManager.isActualTransactionActive();
            log.info("Hello init @PostConstruct tx active={}" , isActive);
        }
    }
}
