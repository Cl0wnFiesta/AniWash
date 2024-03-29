package aniwash.dao;

import aniwash.entity.Product;
import aniwash.entity.localization.LocalizedProduct;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;

import java.util.List;
import java.util.function.Consumer;

/**
 * This class is used to access the database and perform CRUD operations on the Product table.
 *
 * @author rasmushy, lassib
 */
public class ProductDao implements IProductDao {

    public boolean add(Product product) {
        EntityManager em = aniwash.datastorage.DatabaseConnector.getInstance();
        Product p = em.find(Product.class, product.getId());
        if (em.contains(p)) {
            System.out.println("Product already exists with id: " + product.getId());
            return false;
        }
        executeInTransaction(entityManager -> em.persist(product), em);
        return true;
    }

    public List<Product> findAll() {
        EntityManager em = aniwash.datastorage.DatabaseConnector.getInstance();
        return em.createQuery("SELECT p FROM Product p WHERE p.deleted = 0", Product.class).getResultList();
    }

    public Product findById(long id) {
        EntityManager em = aniwash.datastorage.DatabaseConnector.getInstance();
        Product p = em.find(Product.class, id);
        if (p != null && p.isDeleted() > 0) {
            return null;
        }
        return p;
    }

    public Product findByName(String name) {
        EntityManager em = aniwash.datastorage.DatabaseConnector.getInstance();
        Product p = null;
        try {
            LocalizedProduct lp = em.createQuery("SELECT lp FROM LocalizedProduct lp WHERE lp.name = :name AND lp.product.deleted = 0", LocalizedProduct.class).setParameter("name", name).getSingleResult();
            p = lp.getProduct();
        } catch (NoResultException e) {
            System.out.println("No product found with name: " + name);
        }
        return p;
    }

    public boolean update(Product product) {
        EntityManager em = aniwash.datastorage.DatabaseConnector.getInstance();
        Product p = em.find(Product.class, product.getId());
        if (!em.contains(p)) {
            System.out.println("Product does not exist in database. Id: " + product.getId());
            return false;
        }
        em.getTransaction().begin();
        p.setPrice(product.getPrice());
        p.setStyle(product.getStyle());
        em.getTransaction().commit();
        return true;
    }

    public boolean deleteById(long id) {
        EntityManager em = aniwash.datastorage.DatabaseConnector.getInstance();
        Product p = em.find(Product.class, id);
        if (em.contains(p) && p.isDeleted() == 0) {
            em.getTransaction().begin();
            p.setDeleted();
            em.getTransaction().commit();
            //executeInTransaction(entityManager -> em.remove(p), em);
            return true;
        }
        System.out.println("Product does not exist for id: " + id);
        return false;
    }

    public Product findNewest() {
        EntityManager em = aniwash.datastorage.DatabaseConnector.getInstance();
        Product p = null;
        try {
            p = em.createQuery("SELECT p FROM Product p ORDER BY p.id DESC", Product.class).getSingleResult();
        } catch (NoResultException e) {
            System.out.println("No product found");
        }
        return p;
    }

    public void executeInTransaction(Consumer<EntityManager> action, EntityManager em) {
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            action.accept(em);
            tx.commit();
        } catch (RuntimeException e) {
            tx.rollback();
            throw e;
        }
    }

}
