package org.orangehrm.customexception;

public class FrameworkException extends RuntimeException{
    public FrameworkException(String messg){
        super(messg);
        printStackTrace();
    }
}
