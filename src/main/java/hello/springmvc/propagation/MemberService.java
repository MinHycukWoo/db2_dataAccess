package hello.springmvc.propagation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    private final LogRepository logRepository;

    @Transactional
    public void joinV1(String username){
        Member member = new Member(username);
        Log logMessage = new Log(username);

        //트랜잭션을 각각 사용하는 예제이다
        log.info("== memberRepository 호출 시작 ==");
        memberRepository.save(member);
        log.info("== memberRepository 호출 종료 ==");

        log.info("== logRepository 호출 시작 ==");
        logRepository.save(logMessage);
        log.info("== logRepository 호출 종료 ==");
    }

    @Transactional
    public void joinV2(String username){
        Member member = new Member(username);
        Log logMessage = new Log(username);

        //트랜잭션을 각각 사용하는 예제이다
        log.info("== memberRepository 호출 시작 ==");
        memberRepository.save(member);
        log.info("== memberRepository 호출 종료 ==");

        log.info("== logRepository 호출 시작 ==");
        try{
            logRepository.save(logMessage);
        }catch(RuntimeException e){
            //로그 저장실패라는 언체크 예외가 발생해서 전체 롤백이 되야 하지만
            //예외상황이라도 정상진행시키겟다는 구분
            log.info("log 저장에 실패했습니다. logMessage ={}" , logMessage.getMessage());
            log.info("정상흐름 반환");
        }
        //logRepository.save(logMessage);
        log.info("== logRepository 호출 종료 ==");
    }
}
