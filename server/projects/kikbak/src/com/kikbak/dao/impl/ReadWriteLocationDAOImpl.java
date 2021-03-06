package com.kikbak.dao.impl;


import org.springframework.stereotype.Repository;

import com.kikbak.dao.ReadWriteLocationDAO;
import com.kikbak.dao.generic.GenericDAOImpl;
import com.kikbak.dto.Location;

@Repository
public class ReadWriteLocationDAOImpl extends GenericDAOImpl<Location, Long> implements ReadWriteLocationDAO{

}
