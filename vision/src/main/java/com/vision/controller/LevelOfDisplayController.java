package com.vision.controller;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vision.exception.JSONExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.Constants;
import com.vision.vb.LevelOfDisplayUserVb;
import com.vision.vb.LevelOfDisplayVb;
import com.vision.wb.LevelOfDisplayWb;

import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value="levelOfDisplay")
public class LevelOfDisplayController{
	
	@Autowired
	LevelOfDisplayWb levelOfDisplayWb;
	
	@RequestMapping(path = "/userGroupProfile", method = RequestMethod.GET)
	@ApiOperation(value = "User Group Profile Listing", notes = "Returns list of user group and profile from profile privileges", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getUserGroupProfile() {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			List<LevelOfDisplayVb> queryList = levelOfDisplayWb.getQueryUserGroupProfile();
			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,
					"Listing User Group and its User Profile", queryList);
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	/*-----------------------------------------User Group SERVICE------------------------------------------------------------------------------------*/
	@RequestMapping(path = "/userGroup", method = RequestMethod.GET)
	@ApiOperation(value = "User Group Listing", notes = "Returns list of user group from profile privileges", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getUserGroup() {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			List<Object> queryList = levelOfDisplayWb.getQueryUserGroup();
			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Listing User Group", queryList);
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-----------------------------------------User Group based User Profile SERVICE------------------------------------------------------------------------------------*/
	@RequestMapping(path = "/userGroupbasedProfile/{userGroup}", method = RequestMethod.GET)
	@ApiOperation(value = "User Group based Profile Listing", notes = "Returns list of user group based profile from profile privileges", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getUserGroupBasedProfile(@PathVariable("userGroup") String userGroup) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			List<Object> queryList = levelOfDisplayWb.getQueryUserGroupBasedProfile(userGroup);
			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Listing User Group", queryList);
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	/*-----------------------------------------Vision User List based on User Group and User Profile------------------------------------------------------------------------------------*/
	@RequestMapping(path = "/userListExcludingUserGrpAndProf", method = RequestMethod.POST)
	@ApiOperation(value = "User Listing based On User Group and Profile", notes = "Returns list of users based on user group and profile from vision users table", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getUserListExcludingUserGrpProf(@RequestBody List<LevelOfDisplayUserVb> usersVbList) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			List<Object> queryList = levelOfDisplayWb.getUserListExcludingUserGrpProf(usersVbList);
			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Listing User Group", queryList);
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
}