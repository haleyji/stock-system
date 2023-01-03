package com.example.stock.service;

import com.example.stock.domain.Stock;
import com.example.stock.repository.StockRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void addStockItem(){
        Stock stock = Stock.builder().id(1L).quantity(100L).build();
        stockRepository.saveAndFlush(stock);
    }

    @AfterEach
    public void deleteAllStockItems(){
        stockRepository.deleteAll();
    }

    @Test
    @DisplayName("재고가 100개인 아이템을 100번 주문 -> 동시성 이슈 발생으로 재고가 0이 아님")
    public void test1() throws InterruptedException {
        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(32);

        CountDownLatch count = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Long stockId = 1L;
            executorService.submit(() -> {
                try{
                    stockService.decrease(stockId, 1L);
                } finally {
                    count.countDown();
                }
            });
        }
        count.await();

        Assertions.assertNotEquals(0L, stockRepository.findById(1L).orElseThrow().getQuantity());

    }


    @Test
    @DisplayName("재고가 100개인 아이템을 100번 주문 with pessimistic lock 을 사용")
    public void test2() throws InterruptedException {
        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(32);

        CountDownLatch count = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Long stockId = 1L;
            executorService.submit(() -> {
                try{
                    stockService.decrease_pessimistic_lock(stockId, 1L);
                } finally {
                    count.countDown();
                }
            });
        }
        count.await();

        Assertions.assertEquals(0L, stockRepository.findById(1L).orElseThrow().getQuantity());
    }

    @Test
    @DisplayName("재고가 100개인 아이템을 100번 주문 with optimistic lock 을 사용")
    public void test3() throws InterruptedException {
        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(32);

        CountDownLatch count = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Long stockId = 1L;
            executorService.submit(() -> {
                try{
                    try {
                        decrease_optimistic_lock_facade(stockId, 1L);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                } finally {
                    count.countDown();
                }
            });
        }
        count.await();

        Assertions.assertEquals(0L, stockRepository.findById(1L).orElseThrow().getQuantity());
    }


    @Test
    @DisplayName("재고가 100개인 아이템을 100번 주문 with named lock 사용")
    public void test4() throws InterruptedException {
        int threadCount = 100;

        ExecutorService executorService = Executors.newFixedThreadPool(32);

        CountDownLatch count = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            Long stockId = 1L;
            executorService.submit(() -> {
                try{
                    stockService.decrease_named_lock(stockId, 1L);
                } finally {
                    count.countDown();
                }
            });
        }
        count.await();

        Assertions.assertNotEquals(0L, stockRepository.findById(1L).orElseThrow().getQuantity());

    }


    public void decrease_optimistic_lock_facade(Long id, Long quantity) throws InterruptedException {
        while (true) {
            try {
                stockService.decrease_optimistic_lock(id, quantity);

                break;
            } catch (Exception e) {
                Thread.sleep(58);
            }
        }
    }

}