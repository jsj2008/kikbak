package com.kikbak.client.dao.impl;

import org.springframework.stereotype.Repository;

import com.kikbak.client.dao.ReadWriteUserDAO;
import com.kikbak.dao.generic.GenericDAOImpl;
import com.kikbak.dto.User;

@Repository
public class ReadWriteUserDAOImpl extends GenericDAOImpl<User, Long> implements ReadWriteUserDAO {


}
