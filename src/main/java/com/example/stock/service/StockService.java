package com.example.stock.service;

import com.example.stock.domain.Stock;

import com.example.stock.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.transaction.annotation.Propagation.*;

@Service
public class StockService {

    private final StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    public void decrease(Long id, Long quantity){
        Stock stock = stockRepository.findById(id).orElseThrow(() ->new RuntimeException("아이템이 존재하지 않습니다"));

        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);
    }

    @Transactional
    public void decrease_pessimistic_lock(Long id, Long quantity){
        //db에 직접 lock 을 거는 방식
        Stock stock = stockRepository.findByIdWithPessimisticLock(id);

        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);
    }

    @Transactional
    public void decrease_optimistic_lock(Long id, Long quantity) throws InterruptedException {
        //lock을 걸지않고 version 을 사용하는 방식
        //실패했을 경우를 대비해 while 문 을 돌리고 Thread.sleep 을 이용하여 재시도 할 수 있게 함

        Stock stock = stockRepository.findByIdWithOptimisticLock(id);

        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);

    }


    @Transactional
    public void decrease_named_lock(Long id, Long quantity){

        try {
            stockRepository.getLock(id.toString());

            Stock stock = stockRepository.findById(id).orElseThrow(() -> new RuntimeException("아이템이 존재하지 않습니다"));
            stock.decrease(quantity);
            stockRepository.saveAndFlush(stock);
        } finally {
            stockRepository.releaseLock(id.toString());
        }
    }

}
