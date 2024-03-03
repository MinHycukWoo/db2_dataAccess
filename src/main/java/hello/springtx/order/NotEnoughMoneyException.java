package hello.springtx.order;

public class NotEnoughMoneyException extends Exception{
    //Exception을 상속받았기 떄문에 체크예외라서 커밋이 될거다.
    public NotEnoughMoneyException(String message){
        super(message);
    }
}
