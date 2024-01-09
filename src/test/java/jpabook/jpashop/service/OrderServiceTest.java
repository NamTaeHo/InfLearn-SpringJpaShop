package jpabook.jpashop.service;

import jakarta.persistence.EntityManager;
import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;
import org.junit.jupiter.api.Test;


import jpabook.jpashop.domain.item.ItemBook;


import java.awt.print.Book;

import static org.junit.Assert.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired
    EntityManager em;

    @Autowired
    OrderService orderService;

    @Autowired
    OrderRepository orderRepository;

    @Test
    public void 상품주문() throws Exception {
        //given
        Member member = createMember();

        ItemBook itemBook = createItemBook("시골 JPA", 10000, 10);

        int orderCount = 2;



        //when
        Long orderId = orderService.order(member.getId(), itemBook.getId(), orderCount);


        Order getOrder = orderRepository.findOne(orderId);

        //then
        assertEquals("상품 주문시 상태는 ORDER", OrderStatus.ORDER, getOrder.getStatus());
        assertEquals("주문한 상품수가 정확해야 한다", 1, getOrder.getOrderItems().size());
        assertEquals("주문 가격은 가격 * 수량이다", 10000 * orderCount, getOrder.getTotalPrice());
        assertEquals("주문 수량 만큼 재고가 줄어야 한다", 8, itemBook.getStockQuantity());

    }

    @Test
    public void 상품주문_재고수량초과() throws Exception {
        //given
        Member member = createMember();
        ItemBook itemBook = createItemBook("시골 JPA", 10000, 10);

        int orderCount = 9;


        //when
        orderService.order(member.getId(), itemBook.getId(), orderCount);


        //then
        Assertions.assertThatThrownBy(() -> orderService.order(member.getId(), itemBook.getId(), orderCount))
                .isInstanceOf(NotEnoughStockException.class);

    }


    @Test
    public void 주문취소() throws Exception {
        //given
        Member member = createMember();
        ItemBook itemBook = createItemBook("시골 JPA", 10000, 10);

        int orderCount = 2;

        //when
        Long orderId = orderService.order(member.getId(), itemBook.getId(), orderCount);

        orderService.cancelOrder(orderId);

        //then
        Order getOrder = orderRepository.findOne(orderId);

        assertEquals("주문취소시 상태는 CANCEL이다.",OrderStatus.CANCEL,getOrder.getStatus());
        assertEquals("주문이 취소된 상품은 그만큼 재고가 증가해야 한다.",10, itemBook.getStockQuantity());


    }


    private ItemBook createItemBook(String name, int price, int stockQuantity) {
        ItemBook itemBook = new ItemBook();
        itemBook.setName(name);
        itemBook.setPrice(price);
        itemBook.setStockQuantity(stockQuantity);
        em.persist(itemBook);
        return itemBook;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "경기", "123-123"));
        em.persist(member);
        return member;
    }

}