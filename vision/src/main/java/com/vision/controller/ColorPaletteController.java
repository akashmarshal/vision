/* Dashboard and Widget configuration*/
package com.vision.controller;

import java.util.List;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.vision.exception.ExceptionCode;
import com.vision.exception.JSONExceptionCode;
import com.vision.util.Constants;
import com.vision.util.ValidationUtil;
import com.vision.vb.VrdObjectPropertiesVb;
import com.vision.wb.ColorPaletteWb;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("colorPalette")
@Api(value="colorPalette" , description="Dashboard and Widget configuration")
public class ColorPaletteController {
	
	@Autowired
	ColorPaletteWb colorPaletteWb;
	
 
	/*-------------------------------------LISTS ALL TYPES OF CHARTS-------------------------------------------*/
	@RequestMapping(path = "/getChartTypes", method = RequestMethod.POST)
	@ApiOperation(value = "Listing of Chart Types", notes = "Returns list of all type of charts", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getChartTypes(@RequestBody VrdObjectPropertiesVb designVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			if (!ValidationUtil.isValid(designVb.getVrdObjectID())) {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION,
						"VrdObjectID parameter is missing", null);
			} else {
				JSONArray queryList = colorPaletteWb.findChartTypes(designVb);
				if (queryList.length() > 0) {
					jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,
							queryList.length() + " Type of Charts", queryList.toString());
				} else {
					jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No Chart Types found",
							null);
				}
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (Exception rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	
	@RequestMapping(path="/getActiveColors" , method=RequestMethod.GET)
	@ApiOperation(value = "give colors palettes which are active ", notes = "give colors palettes which are active ", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getActiveColorPaletteFromObjProperties() {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			List<VrdObjectPropertiesVb> designObj = colorPaletteWb.findActiveColorPaletteFromObjProperties();
			if (ValidationUtil.isValidList(designObj)) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION,
						designObj.size() + " Lists of Active Colors", designObj);
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, "No Active Colors found",
						null);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}

		catch (Exception e) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, e.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}

	}
	
	@RequestMapping(path="/addOrModifyColor", method=RequestMethod.POST)
	@ApiOperation(value = "add or modify the color palette", notes = "add or modify the color palette", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> addOrModifyColorPalette(@RequestBody VrdObjectPropertiesVb vcReportObj){
		ExceptionCode exceptionCode=null;
		JSONExceptionCode jsonExceptionCode=null;
		try {
		vcReportObj.setVrdObjectID("Palette");
		if ("add".equalsIgnoreCase(vcReportObj.getWidgetOperation().trim().toLowerCase()))
		exceptionCode = colorPaletteWb.addColorPaletteFromObjProperties(vcReportObj);
		else
		exceptionCode = colorPaletteWb.updateColorPaletteFromObjProperties(vcReportObj);
		if(exceptionCode.getErrorCode() == 1)
			jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, vcReportObj.getWidgetOperation()+"Operation Successful",vcReportObj);
		else 
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, vcReportObj.getWidgetOperation()+"Operation Failed",null);
		return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK); 
		}
		catch(Exception ex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, ex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		}
	}
	
	
}
