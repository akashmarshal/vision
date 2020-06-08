package com.vision.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vision.exception.ExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.vb.VisionUsersVb;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

//@Controller
//@RestController
@RestController
@RequestMapping(value = "/studentController")
@Api(value="onlinestore", description="Operations pertaining to products in Online Store")
//@RestController("/allFormats")
public class StudentController {
	
	@GetMapping(value = "/readUser/{userLoginId}")
	public ResponseEntity<List<VisionUsersVb>> getStudent(@PathVariable("userLoginId") String userLoginId){
		try {
			VisionUsersVb vb = new VisionUsersVb();
			vb.setUserLoginId(userLoginId);
			return new ResponseEntity<List<VisionUsersVb>>(HttpStatus.OK);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	
	/* CREATE */
	@CrossOrigin(origins = "*")
	@RequestMapping(value = "/createStudent", method = RequestMethod.POST)
	@ApiOperation(value = "View a list of available products",
			notes = "Multiple status values can be provided with comma seperated strings",
			response = ResponseEntity.class)
	public ResponseEntity<ExceptionCode> createStudent(){
		HttpStatus status = HttpStatus.CREATED;
		try {
			ExceptionCode ec = new ExceptionCode();
			return new ResponseEntity<ExceptionCode>(ec, status);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}
	
	/* READ */
	/*@CrossOrigin(origins = "*")
	@RequestMapping(value = "/readStudent/{id}", method = RequestMethod.GET)
	public ResponseEntity<StudentVb> getStudent(@PathVariable("id") String id ){
		try {
			StudentVb vb = new StudentVb();
			//Thread.sleep(10000);
			vb.setId(Integer.parseInt(id));
			return new ResponseEntity<StudentVb>(getWb().getStudentData(vb), HttpStatus.OK);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}*/
	
	/* READ ALL */
	/*@CrossOrigin(origins = "*")
	@RequestMapping(value = "/student", method = RequestMethod.GET)
	public ResponseEntity<List<StudentVb>> getAllStudent(){
		HttpStatus status = HttpStatus.OK;
		try {
			ExceptionCode ec = getWb().getAllData();
			//Thread.sleep(10000);
			if(ec.getErrorCode()!=Constants.SUCCESSFUL_OPERATION){
				status = HttpStatus.NO_CONTENT;
			}
			return new ResponseEntity<List<StudentVb>>((List<StudentVb>)ec.getResponse(), status);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}*/
	
	/* DELETE */
	/*@CrossOrigin(origins = "*")
	@RequestMapping(value = "/deleteStudent/{id}", method = RequestMethod.DELETE)
	public ResponseEntity<Integer> deleteStudent(@PathVariable("id") String id ){
		try {
			StudentVb vb = new StudentVb();
			//Thread.sleep(10000);
			vb.setId(Integer.parseInt(id));
			return new ResponseEntity<Integer>(getWb().deleteStudentData(vb), HttpStatus.OK);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}*/
	
	/* UPDATE */
	/*@CrossOrigin(origins = "*")
	@RequestMapping(value = "/updateStudent", method = RequestMethod.PATCH)
	public ResponseEntity<ExceptionCode> updateStudent(@RequestBody StudentVb vb){
		HttpStatus status = HttpStatus.CREATED;
		try {
			ExceptionCode ec = getWb().updateStudent(vb);
			if(ec.getErrorCode()!=Constants.SUCCESSFUL_OPERATION)
				status = HttpStatus.EXPECTATION_FAILED;
			return new ResponseEntity<ExceptionCode>(ec, status);
		}catch(Exception e) {
			e.printStackTrace();
			throw new RuntimeCustomException(e.getMessage());
		}
	}*/
	
}
