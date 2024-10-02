package org.chaostocosmos.leap.spring.repository;

import org.chaostocosmos.leap.spring.entity.Users;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

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
