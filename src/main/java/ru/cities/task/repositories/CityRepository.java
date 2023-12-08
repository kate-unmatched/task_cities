package ru.cities.task.repositories;

import ru.cities.task.entity.City;

import java.util.Collection;
import java.util.List;

public interface CityRepository extends AbstractRepository<City> {
    List<City> findAllByNameIn(Collection<String> cities);
}

/*
@Component
public class CityRepository implements AbstractRepository<City> {
    private final SessionFactory sessionFactory;

    public CityRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    @Override
    public City findById(Long id) {
        try (Session session = sessionFactory.openSession()) {
            return session.get(City.class, id);
        }
    }

    @Override
    public List<City> findAll() {
        try (Session session = sessionFactory.openSession()) {
            return session.createQuery("FROM City", City.class).list();
        }
    }

    @Override
    public void save(City city) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.save(city);
            transaction.commit();
        }
    }

    @Override
    public void update(City city) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            session.update(city);
            transaction.commit();
        }
    }

    @Override
    public void delete(Long id) {
        try (Session session = sessionFactory.openSession()) {
            Transaction transaction = session.beginTransaction();
            City city = session.get(City.class, id);
            if (city != null) {
                session.delete(city);
            }
            transaction.commit();
        }
    }
}
*/