package com.game.repository;

import com.game.entity.Player;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.query.NativeQuery;
import org.hibernate.query.Query;
import org.springframework.stereotype.Repository;

import javax.annotation.PreDestroy;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

@Repository(value = "db")
public class PlayerRepositoryDB implements IPlayerRepository {

    private final SessionFactory sessionFactory;
    public PlayerRepositoryDB() {
        Properties properties = new Properties();
        //properties.put(Environment.DRIVER, "com.mysql.cj.jdbc.Driver");
        //properties.put(Environment.URL, "jdbc:mysql://localhost:3306/rpg");
        properties.put(Environment.DEFAULT_SCHEMA, "rpg");
        properties.put(Environment.DIALECT, "org.hibernate.dialect.MySQLDialect");
        properties.put(Environment.USER, "root");
        properties.put(Environment.PASS, "mysql");
        properties.put(Environment.HBM2DDL_AUTO, "update");
        //properties.put(Environment.SHOW_SQL, "true");
        properties.put(Environment.DRIVER, "com.p6spy.engine.spy.P6SpyDriver");
        properties.put(Environment.URL, "jdbc:p6spy:mysql://localhost:3306/rpg");

        sessionFactory = new Configuration()
                .setProperties(properties)
                .addAnnotatedClass(Player.class)
                .buildSessionFactory();
    }

    @Override
    public List<Player> getAll(int pageNumber, int pageSize) {
        try(Session session = sessionFactory.openSession()) {
            Query<Player> q = session.createNativeQuery("select * from rpg.player limit ?1 offset ?2", Player.class);
            q.setParameter(1, pageSize);
            q.setParameter(2, pageNumber * pageSize);
            return q.getResultList();
        }
    }

    @Override
    public int getAllCount() {
        try(Session session = sessionFactory.openSession()) {
            return ((Long)session.createNamedQuery("PLAYERS_COUNT").getSingleResult()).intValue();
        }
    }

    @Override
    public Player save(Player player) {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.persist(player);
            transaction.commit();
        }
        return player;
    }

    @Override
    public Player update(Player player) {
        try(Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.merge(player);
            transaction.commit();
        }
        return player;
    }

    @Override
    public Optional<Player> findById(long id) {
        try(Session session = sessionFactory.openSession()) {
            return session.byId(Player.class).loadOptional(id);
        }
    }

    @Override
    public void delete(Player player) {
        try(Session session = sessionFactory.openSession()) {
            session.remove(player);
        }
    }

    @PreDestroy
    public void beforeStop() {
        sessionFactory.close();
    }
}