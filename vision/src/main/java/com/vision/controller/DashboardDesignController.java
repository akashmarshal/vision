package com.vision.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.vision.exception.ExceptionCode;
import com.vision.exception.JSONExceptionCode;
import com.vision.exception.RuntimeCustomException;
import com.vision.util.CommonUtils;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.DSConnectorVb;
import com.vision.vb.DashboardDesignVb;
import com.vision.vb.DashboardLODWrapperVb;
import com.vision.vb.DsConnectorLODWrapperVb;
import com.vision.vb.VcConfigMainCRUDVb;
import com.vision.vb.VcConfigMainVb;
import com.vision.vb.WidgetDesignVb;
import com.vision.wb.DashboardDesignWb;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController 
@RequestMapping(value="dashboardDesign")
@Api(value="dashboardDesign",description="Dashboard Design Operations")
public class DashboardDesignController {
	
	@Autowired
	DashboardDesignWb dashboardwb;
	
	@RequestMapping(value="/add", method = RequestMethod.POST)
	public ResponseEntity<JSONExceptionCode> addDashBoardData(@RequestBody DashboardDesignVb dashBoardVb){
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			if(ValidationUtil.isValid(dashBoardVb.getDashboardDesignJson())) {
				exceptionCode = CommonUtils.jsonToXml(dashBoardVb.getDashboardDesignJson());
				if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					dashBoardVb.setDashboardDesign(exceptionCode.getResponse()+"");
				}
			} else {
				exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
				exceptionCode.setErrorMsg("No valid data found in request");
			}
			if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				exceptionCode = dashboardwb.insertRecord(dashBoardVb);
			}
			
			if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(), null, exceptionCode.getOtherInfo()); 
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(), exceptionCode.getOtherInfo()); 
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch(Exception e) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, e.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	@RequestMapping(path = "/get", method = RequestMethod.POST)
	@ApiOperation(value = "Get dashboard based on dashboard ID", notes = "Get dashboard based on dashboard ID from dashboard table", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getDashBoardData(@RequestBody DashboardDesignVb dashboardVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			exceptionCode = dashboardwb.getDashboard(dashboardVb);
			if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				dashboardVb = (DashboardDesignVb) exceptionCode.getResponse();
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, null, dashboardVb, exceptionCode.getOtherInfo()); 
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(), exceptionCode.getOtherInfo()); 
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch(Exception e) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, e.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	@RequestMapping(path = "/modify", method = RequestMethod.POST)
	@ApiOperation(value = "modify Dashboard Data", notes = "modify Dashboard Data", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> updateDashboardData(@RequestBody DashboardDesignVb vObject) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			if(!ValidationUtil.isValid(vObject.getDashboardDesignJson()) || !ValidationUtil.isValid(vObject.getFilterContextJson()) ) {
				exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
				exceptionCode.setErrorMsg("No valid data found in request");
			}
			if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				exceptionCode = dashboardwb.modifyRecord(vObject);
			}
			
			if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(), null, exceptionCode.getOtherInfo()); 
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(), exceptionCode.getOtherInfo()); 
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch(Exception e) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, e.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}

	@RequestMapping(path = "/getAll", method = RequestMethod.GET)
	@ApiOperation(value = "Get all dashboards", notes = "Get all dashboards for listing", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getAll() {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			exceptionCode = dashboardwb.getAllDashboardList();
			if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(), exceptionCode.getResponse(), exceptionCode.getOtherInfo()); 
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(), exceptionCode.getOtherInfo()); 
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch(Exception e) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, e.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	

	/*-------------------------------------DELETE VISION CATALOG SERVICE-------------------------------------------*/
	@RequestMapping(path = "/delete", method = RequestMethod.POST)
	@ApiOperation(value = "Delete Dashboard", notes = "Delete a Dashboard ", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> deleteVcCatalog(@RequestBody DashboardDesignVb dashboardVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			dashboardVb.setRecordIndicator(0);
			ExceptionCode exceptionCode = dashboardwb.deleteRecord(dashboardVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	
	/*-------------------------------------SAVEALL ADD/MODIFY - DASHBOARD AND WIDGET DATAS SERVICE-------------------------------------------*/
	@RequestMapping(value = "/saveAll", method = RequestMethod.POST)
	@ApiOperation(value = "Saves all Dashboard and Widget Properties", notes = "Saves all Dashboard and Widget Properties", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> saveAllVcTableColumnRelation(
			@RequestBody DashboardDesignVb vcMainVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			exceptionCode = dashboardwb.doSaveOperationsWIP(vcMainVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Dashboard save successful",vcMainVb);
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),vcMainVb);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}


	/*-----------------------------------------Publish Catalog SERVICE------------------------------------------------------------------------------------*/
	@RequestMapping(path = "/dashboardPublish", method = RequestMethod.POST)
	@ApiOperation(value = "dashboardPublish Publish", notes = "Move work in progress data to audit for publish", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> dashboardPublish(@RequestBody DashboardDesignVb dashboardvb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			exceptionCode = dashboardwb.doPublishToMain(dashboardvb);
			if (exceptionCode != null && exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getOtherInfo());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}


	@RequestMapping(path = "/saveLevelOfDisplay", method = RequestMethod.POST)
	@ApiOperation(value = "Dashboard - Level Of Display", notes = "save level of display for Dashboard", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> levelOfDisplay(@RequestBody DashboardLODWrapperVb dsLODWrapperVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			DashboardDesignVb dsConnectorVb = dsLODWrapperVb.getMainModel();
				exceptionCode = dashboardwb.doInsertRecordForAccessControl(dsLODWrapperVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Share Successful",
						null);
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						null);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	

	@RequestMapping(path = "/updatePrivileges", method = RequestMethod.POST)
	@ApiOperation(value = "Save privileges for dashboard", notes = "Save privileges for dashboard", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> dashboardPrivileges(@RequestBody DashboardDesignVb dashboardVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
				exceptionCode = dashboardwb.doInsertRecordForPrivileges(dashboardVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "save Privileges Successfull",
						exceptionCode.getResponse());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						null);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	

	@RequestMapping(path = "/getLevelOfDisplay", method = RequestMethod.POST)
	@ApiOperation(value = "Dashboard - Level Of Display", notes = "get level of display for Dashboard", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getlevelOfDisplay(@RequestBody DashboardLODWrapperVb dsLODWrapperVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
				exceptionCode = dashboardwb.getLODForDashboard(dsLODWrapperVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Share Successful",
						exceptionCode.getResponse());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getResponse());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}	
	

	@RequestMapping(path = "/userListExcludingUserGrpAndProf", method = RequestMethod.POST)
	@ApiOperation(value = "Dashboard - Level Of Display", notes = "get level of display for Dashboard", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getuserListExcludingUserGrpAndProf(@RequestBody DashboardLODWrapperVb dashboardVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
				exceptionCode = dashboardwb.getuserListExcludingUserGrpAndProf(dashboardVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Share Successful",
						exceptionCode.getResponse());
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(),
						exceptionCode.getResponse());
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}	
}
