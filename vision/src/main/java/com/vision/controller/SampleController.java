package com.vision.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.vision.dao.VisionUsersDao;
import com.vision.exception.RuntimeCustomException;
import com.vision.vb.VisionUsersVb;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "sample")
@Api(value="onlinestore", description="Operations pertaining to products")
public class SampleController {
	
	@Autowired
	VisionUsersDao dao;
	
	public VisionUsersDao getDao() {
		return dao;
	}

	public void setDao(VisionUsersDao dao) {
		this.dao = dao;
	}

	@GetMapping(value = "/readUser/{userLoginId}")
	@ApiOperation(value = "View a list of available products",
	notes = "Multiple status values can be provided with comma seperated strings",
	response = ResponseEntity.class)
	public ResponseEntity<List<VisionUsersVb>> getStudent(@PathVariable("userLoginId") String userLoginId){
		try {
			VisionUsersVb vb = new VisionUsersVb();
			vb.setUserLoginId(userLoginId);
			return new ResponseEntity<List<VisionUsersVb>>(getDao().getActiveUserByUserLoginId(vb), HttpStatus.OK);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	
	@GetMapping(value = "/login")
	public ResponseEntity<List<VisionUsersVb>> getKamal(){
		try {
			VisionUsersVb vb = new VisionUsersVb();
			vb.setUserLoginId("kamal");
			return new ResponseEntity<List<VisionUsersVb>>(getDao().getActiveUserByUserLoginId(vb), HttpStatus.OK);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
}
