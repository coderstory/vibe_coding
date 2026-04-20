package cn.coderstory.springboot.order.service;

public interface TimeoutCancelService {
    void scheduleOrderTimeoutCheck();
}