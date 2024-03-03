package hello.springmvc.propagation;


import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.interceptor.DefaultTransactionAttribute;

import javax.sql.DataSource;

@Slf4j
@SpringBootTest
public class BasicTxTest {
    
    @Autowired
    PlatformTransactionManager txManager;
    
    @TestConfiguration
    static class Config{

        //트랜잭션 매니저를 원래 스프링이 자동등록해주는데 직접등록한 경우

        @Bean
        public PlatformTransactionManager transactionManager(DataSource dataSource){
            log.info("dataSource {}" , dataSource);
            return new DataSourceTransactionManager(dataSource);
        }
    }
        @Test
        void commit(){
            log.info("트랜잭션 시작");
            TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());
            //트랜잭션 시작할때 커넥션을 가져온다
            log.info("트랜잭션 커밋 시작");
            txManager.commit(status);
            //커밋도하고 JDBC커넥션도 되돌려준다.
            log.info("트랜잭션 커밋 완료");
        }

        @Test
        void rollback(){
            log.info("트랜잭션 시작");
            TransactionStatus status = txManager.getTransaction(new DefaultTransactionAttribute());
            //트랜잭션 시작할때 커넥션을 가져온다
            log.info("트랜잭션 롤백 시작");
            txManager.rollback(status);
            //커밋도하고 JDBC커넥션도 되돌려준다.
            log.info("트랜잭션 롤백 완료");
        }

        @Test
        void double_commit(){
            log.info("트랜잭션1 시작");
            TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
            log.info("트랜잭션1 커밋 시작");
            txManager.commit(tx1);

            log.info("트랜잭션2 시작");
            TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
            log.info("트랜잭션2 커밋 시작");
            txManager.commit(tx2);
        }

    @Test
    void double_commit_rollback(){
        log.info("트랜잭션1 시작");
        TransactionStatus tx1 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션1 커밋 시작");
        txManager.commit(tx1);

        log.info("트랜잭션2 시작");
        TransactionStatus tx2 = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("트랜잭션2 커밋 시작");
        txManager.rollback(tx2);
    }


    @Test
    void inner_commit(){
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}" , outer.isNewTransaction());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("inner.isNewTransaction()={}",inner.isNewTransaction());

        //.isNewTransaction() 는 해당 트랜잭션이 이 물리트린잭션에서 처음 실행된 트랜잭션인지를 확인
        //현재 상황은 처음 트랜잭션이 생성되고 commit이나 rollback 이 되기전에 또다른 트랜잭션이
        //실행된 상황

        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner);

        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer);
    }

    @Test
    void outer_rollback(){
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());


        log.info("내부 트랜잭션 커밋");
        txManager.commit(inner);

        log.info("외부 트랜잭션 커밋");
        txManager.rollback(outer);
    }


    @Test
    void inner_rollback(){
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());

        log.info("내부 트랜잭션 시작");
        TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());


        log.info("내부 트랜잭션 롤백");
        txManager.rollback(inner); //rollback-only 표시

        log.info("외부 트랜잭션 커밋");
        //txManager.commit(outer);
        Assertions.assertThatThrownBy(()-> txManager.commit(outer))
                .isInstanceOf(UnexpectedRollbackException.class);
    }

    @Test
    void inner_rollback_requires_new(){
        log.info("외부 트랜잭션 시작");
        TransactionStatus outer = txManager.getTransaction(new DefaultTransactionAttribute());
        log.info("outer.isNewTransaction()={}" , outer.isNewTransaction());//true

        log.info("내부 트랜잭션 시작");
        DefaultTransactionAttribute definition = new DefaultTransactionAttribute();
        definition.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        //이 옵션의 기본값은 PROPAGATION_REQUIRED 로
        //기존 트랜잭션에 참여 하는것이고
        //PROPAGATION_REQUIRES_NEW는 기존 트랙잭션이 있어도 무시하고 트랜잭션을 하나 더 만드는것
        //*TransactionStatus inner = txManager.getTransaction(new DefaultTransactionAttribute());
        TransactionStatus inner = txManager.getTransaction(definition);
        log.info("outer.isNewTransaction()={}" , inner.isNewTransaction()); //true

        log.info("내부 트랜잭션 롤백");
        txManager.rollback(inner); //롤백
        
        log.info("외부 트랜잭션 커밋");
        txManager.commit(outer); //커밋
        

    }
}
