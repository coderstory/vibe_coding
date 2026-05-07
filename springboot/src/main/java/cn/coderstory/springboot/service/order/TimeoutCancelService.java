package cn.coderstory.springboot.service.order;

public interface TimeoutCancelService {
    void scheduleOrderTimeoutCheck();
}