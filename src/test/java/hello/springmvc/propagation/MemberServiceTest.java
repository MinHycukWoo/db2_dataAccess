package hello.springmvc.propagation;

import hello.springmvc.propagation.LogRepository;
import hello.springmvc.propagation.MemberRepository;
import hello.springmvc.propagation.MemberService;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.UnexpectedRollbackException;


@Slf4j
@SpringBootTest
class MemberServiceTest {

    @Autowired
    MemberService memberService;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    LogRepository logRepository;

    /*
    * memberService     @Transactional : OFF
    * memberRepository  @Transactional : ON
    * logRepository     @Transactional : ON . Exception
    * */

    @Test
    void outerTxOff_fail(){
        //given
        String username = "로그예외 , outerTxOff_fail";
        //when
        memberService.joinV1(username);
        //Assertions.assertThatThrownBy(() -> memberService.joinV1(username))
        //        .isInstanceOf(RuntimeException.class);

        //when : 모든 데이터가 정상 저장된다.
        //존재 유무를 확인
        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isEmpty());
    }

    /*
     * memberService     @Transactional : ON    Exception
     * memberRepository  @Transactional : OFF
     * logRepository     @Transactional : OFF .
     * */
    @Test
    void singleTx(){
        //given
        String username = "outerTxOff_fail";
        //when
        //memberService.joinV1(username);
        memberService.joinV1(username);

        //when : 모든 데이터가 정상 저장된다.
        //존재 유무를 확인
        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isPresent());
    }

    /*
     * memberService     @Transactional : ON    Exception
     * memberRepository  @Transactional : ON
     * logRepository     @Transactional : ON .
     * */
    @Test
    void outerTxOn_success(){
        //given
        String username = "outerTxOn_success";
        //when
        //memberService.joinV1(username);
        memberService.joinV1(username);

        //when : 모든 데이터가 정상 저장된다.
        //존재 유무를 확인
        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isPresent());
    }



    /*
     * memberService     @Transactional : ON
     * memberRepository  @Transactional : ON
     * logRepository     @Transactional : ON . Exception
     * */

    @Test
    void outerTxOn_fail(){
        //given
        String username = "로그예외 , outerTxOn_fail";
        //when
        memberService.joinV1(username);
        //Assertions.assertThatThrownBy(() -> memberService.joinV1(username))
        //        .isInstanceOf(RuntimeException.class);

        //when : 모든 데이터가 롤백된다.
        //존재 유무를 확인
        Assertions.assertTrue(memberRepository.find(username).isEmpty());
        Assertions.assertTrue(logRepository.find(username).isEmpty());
    }


    /*
     * memberService     @Transactional : ON
     * memberRepository  @Transactional : ON
     * logRepository     @Transactional : ON . Exception
     * */

    @Test
    void recoverException_fail(){
        //given
        String username = "로그예외 , recoverException_fail";
        //when
        memberService.joinV1(username);
        //Assertions.assertThatThrownBy(() -> memberService.joinV1(username))
        //       .isInstanceOf(UnexpectedRollbackException.class);

        //when : 모든 데이터가 롤백된다.
        //존재 유무를 확인
        Assertions.assertTrue(memberRepository.find(username).isEmpty());
        Assertions.assertTrue(logRepository.find(username).isEmpty());
    }

    /*
     * memberService     @Transactional : ON
     * memberRepository  @Transactional : ON
     * logRepository     @Transactional : ON(Requires_new) . Exception
     * */

    @Test
    void recoverException_success(){
        //given
        String username = "로그예외 , recoverException_success";
        //when
        memberService.joinV2(username);

        //when : 모든 데이터가 롤백된다.
        //존재 유무를 확인
        Assertions.assertTrue(memberRepository.find(username).isPresent());
        Assertions.assertTrue(logRepository.find(username).isEmpty());
    }
}