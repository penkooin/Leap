package org.chaostocosmos.leap.service.repository;

import java.util.List;

import org.chaostocosmos.leap.service.datasource.LeapDataSource;
import org.chaostocosmos.leap.service.entity.Users;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

//@Repository
public class UsersRepository {//implements IUsersRepository {

    private final JdbcTemplate jdbcTemplate;

    public UsersRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    // @Override
    // public List<Users> findByName(String name) {
    //     // Perform the query using the active data source
    //     String sql = "SELECT * FROM your_table WHERE name = ?";
    //     return jdbcTemplate.query(sql, new Object[]{name}, yourEntityRowMapper);
    // }

    // @Override
    // public Users findById(long id) {
    //     return null;
    // }    

    //@Override
    // public void save(Users user) {
        
    // }

    private final RowMapper<Users> yourEntityRowMapper = (resultSet, rowNum) -> {
        Users entity = new Users();        
        
        return entity;
    };    
}
