package se.hkr.java.db;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import se.hkr.java.db.entities.Customer;
import se.hkr.java.db.entities.Order;

import java.time.LocalDate;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        // Create a configuration instance and configure it
        Configuration configuration = new Configuration().configure();

        // Create a session factory from the configuration
        SessionFactory sessionFactory = configuration.buildSessionFactory();

        // Open a session from the session factory
        Session session = sessionFactory.openSession();

        // Create a new Customer instance and save it to the database
        Transaction tx = session.beginTransaction();
        Customer customer = new Customer();
        customer.setName("John Doe");
        customer.setAddress("123 Main St");
        session.persist(customer);

        // Create a new Order instance and associate it with the customer
        Order order = new Order();
        order.setOrderDate(LocalDate.now());
        order.setCustomer(customer);
        session.persist(order);
        tx.commit();

        // Test the entities using HQL (Hibernate Query Language)
        List<Customer> customers = session.createQuery("FROM Customer", Customer.class).list();
        customers.forEach(System.out::println);

        // Fetch all orders
        List<Order> orders = session.createQuery("FROM Order", Order.class).list();
        orders.forEach(o -> System.out.println(o + ", customer: " + o.getCustomer()));

        // This will also fetch the order's customer
        List<Order> ordersWithCustomers = session.createQuery("FROM Order o JOIN FETCH o.customer", Order.class).list();
        ordersWithCustomers.forEach(o -> System.out.println(o + ", customer: " + o.getCustomer()));

        // Fetch all orders more than one month old
        Query<Order> query = session.createQuery("FROM Order o WHERE o.orderDate < :searchDate", Order.class);
        query.setParameter("searchDate", LocalDate.now().minusMonths(1));
        List<Order> oldOrders = query.list();
        oldOrders.forEach(o -> System.out.println(o + ", customer: " + o.getCustomer()));

        // Fetching old orders using the Criteria API instead of HQL
        CriteriaBuilder cb = session.getCriteriaBuilder();
        CriteriaQuery<Order> query2 = cb.createQuery(Order.class);
        Root<Order> root = query2.from(Order.class);
        query2.select(root).where(cb.lessThan(root.get("orderDate"), LocalDate.now().minusMonths(1)));
        List<Order> oldOrders2 = session.createQuery(query2).list();
        oldOrders2.forEach(o -> System.out.println(o + ", customer: " + o.getCustomer()));

        // Close the session and session factory
        session.close();
        sessionFactory.close();
    }
}