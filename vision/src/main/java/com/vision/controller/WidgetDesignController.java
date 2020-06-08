package com.vision.controller;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

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
import com.vision.vb.VcConfigMainLODWrapperVb;
import com.vision.vb.VcForQueryReportFieldsVb;
import com.vision.vb.VcForQueryReportFieldsWrapperVb;
import com.vision.vb.VcForQueryReportVb;
import com.vision.vb.WidgetDesignVb;
import com.vision.vb.WidgetLODWrapperVb;
import com.vision.vb.WidgetWrapperVb;
import com.vision.wb.DesignAnalysisWb;
import com.vision.wb.WidgetDesignWb;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping(value = "widgetDesign")
@Api(value = "widgetDesign", description = "Operations pertaining to Widget Designs")
public class WidgetDesignController {
	@Autowired
	private DesignAnalysisWb designAnalysisWb;
	
	@Autowired
	private WidgetDesignWb widgetDesignWb;
	
	/*-------------------------------------GET ALL DESIGN QUERIES SERVICE-------------------------------------------*/
	@RequestMapping(path = "/getWidgetData", method = RequestMethod.POST)
	@ApiOperation(value = "Get Widget Data", notes = "Returns JSON for Chart rendering", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getWidgetData(@RequestBody WidgetWrapperVb vObject) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode=null;
		String filterTableName = "";
		try {
			WidgetDesignVb widgetDesignVb = new WidgetDesignVb();
			boolean isTableAvailable = false;
			String dataViewName = "";
			
			if(ValidationUtil.isValid(vObject.getMainModel().getWidgetContextJson())) {
				exceptionCode = CommonUtils.jsonToXml(vObject.getMainModel().getWidgetContextJson());
				if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					vObject.getMainModel().setWidgetContext(exceptionCode.getResponse()+"");
				} else {
					exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
				}
			} else {
				exceptionCode = widgetDesignWb.getWidget(vObject.getMainModel());
				if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					widgetDesignVb = (WidgetDesignVb) exceptionCode.getResponse();
					exceptionCode = CommonUtils.XmlToJson(widgetDesignVb.getWidgetContext());
					if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
						widgetDesignVb.setWidgetContextJson(exceptionCode.getResponse()+"");
					}
					exceptionCode = CommonUtils.XmlToJson(widgetDesignVb.getFilterContext());
					if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
						widgetDesignVb.setFilterContextJson(exceptionCode.getResponse()+"");
					}
					vObject.setMainModel(widgetDesignVb);
				}
			}
			
			if(exceptionCode.getErrorCode()!=Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(), null);
				return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
			}
			
			if(ValidationUtil.isValid(vObject.getDataViewName())) {
				exceptionCode = widgetDesignWb.updateWidgetCreationStagingTable(vObject.getDataViewName());
				if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					isTableAvailable = true;
				}
			}
			
			if(!isTableAvailable) {
				VcForQueryReportVb reportVb = new VcForQueryReportVb();
				reportVb.setReportId(vObject.getMainModel().getQueryId());
				exceptionCode = designAnalysisWb.getReportDetailFromReportDefs(reportVb);
				VcForQueryReportFieldsWrapperVb wrapperVb = (VcForQueryReportFieldsWrapperVb) exceptionCode.getResponse();
				if(exceptionCode.getErrorCode()==Constants.SUCCESSFUL_OPERATION) {
					wrapperVb.setHashArray(vObject.getHashArray());
					wrapperVb.setHashValueArray(vObject.getHashValueArray());
				}
				
				Set<String> uniqueTableIdSet = new HashSet<String>();
				
				for(VcForQueryReportFieldsVb rCReportFieldsVb: wrapperVb.getReportFields()) {
					if(ValidationUtil.isValid(rCReportFieldsVb.getTabelId())) {
						uniqueTableIdSet.add(rCReportFieldsVb.getTabelId());
					}
				}
				
				wrapperVb.getMainModel().setBaseTableId(designAnalysisWb.returnBaseTableId(wrapperVb.getMainModel().getCatalogId()));
				wrapperVb.setWidgetFilterList(vObject.getMainModel().getWidgetFilterList());
				exceptionCode = designAnalysisWb.executeCatalogQuery(wrapperVb, uniqueTableIdSet.stream().collect(Collectors.toList()), false, false);
				vObject.getMainModel().setTotalRows(wrapperVb.getMainModel().getTotalRows());
				vObject.getMainModel().setOrderBy(wrapperVb.getMainModel().getOrderBy());
			}
			
			dataViewName = String.valueOf(exceptionCode.getResponse());
			
			if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				if(ValidationUtil.isValidList(vObject.getMainModel().getWidgetFilterList())){
					exceptionCode = widgetDesignWb.returnTableForFilter(vObject.getMainModel().getWidgetFilterList(), dataViewName);
					filterTableName = String.valueOf(exceptionCode.getResponse());
				}
				
				if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					String chartXml = vObject.getMainModel().getWidgetContext();
					
					if("Grid".equalsIgnoreCase(CommonUtils.getValueForXmlTag(chartXml, "ChartType"))) {
						exceptionCode = widgetDesignWb.getGridResponseWithChartXML(vObject,chartXml, ValidationUtil.isValid(filterTableName)?filterTableName:dataViewName);
						vObject.setDataViewName(dataViewName);
						if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
							jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, null, exceptionCode.getResponse(), vObject); 
						} else {
							jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(), vObject); 
						}
					} else {
						exceptionCode = widgetDesignWb.getChartResponseWithChartXML(chartXml, ValidationUtil.isValid(filterTableName)?filterTableName:dataViewName);
						exceptionCode.setOtherInfo(dataViewName);
						if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
							jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, null, String.valueOf(exceptionCode.getResponse()), exceptionCode.getOtherInfo()); 
						} else {
							jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(), null); 
						}
					}
				} else {
					jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(), null);
				}
			} else {
				jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(), null);
			}
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} catch (RuntimeCustomException rex) {
			jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, rex.getMessage(), "");
			return new ResponseEntity<JSONExceptionCode>(jsonExceptionCode, HttpStatus.OK);
		} finally {
			if(ValidationUtil.isValid(filterTableName)) {
				designAnalysisWb.getDesignAnalysisDao().dropTable(filterTableName);
			}
		}
	}
	
	@RequestMapping(path = "/addWidgetData", method = RequestMethod.POST)
	@ApiOperation(value = "Add Widget Data", notes = "Add Widget Data", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> addWidgetData(@RequestBody WidgetDesignVb vObject) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			if(ValidationUtil.isValid(vObject.getWidgetContextJson())) {
				exceptionCode = CommonUtils.jsonToXml(vObject.getWidgetContextJson());
				if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					vObject.setWidgetContext(exceptionCode.getResponse()+"");
				}
			} else {
				exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
				exceptionCode.setErrorMsg("No valid data found in request");
			}
			
			if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION && ValidationUtil.isValid(vObject.getFilterContextJson())) {
				exceptionCode = CommonUtils.jsonToXml(vObject.getFilterContextJson());
				if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					vObject.setFilterContext(exceptionCode.getResponse()+"");
				}
			} else {
				exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
				exceptionCode.setErrorMsg("No valid data found in request");
			}
			if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				exceptionCode = widgetDesignWb.insertRecord(vObject);
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
	
	@RequestMapping(path = "/updateWidgetData", method = RequestMethod.POST)
	@ApiOperation(value = "Update Widget Data", notes = "Update Widget Data", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> updateWidgetData(@RequestBody WidgetDesignVb vObject) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			if(ValidationUtil.isValid(vObject.getWidgetContextJson())) {
				exceptionCode = CommonUtils.jsonToXml(vObject.getWidgetContextJson());
				if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					vObject.setWidgetContext(exceptionCode.getResponse()+"");
				}
			} else {
				exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
				exceptionCode.setErrorMsg("No valid data found in request");
			}
			if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION && ValidationUtil.isValid(vObject.getFilterContextJson())) {
				exceptionCode = CommonUtils.jsonToXml(vObject.getFilterContextJson());
				if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					vObject.setFilterContext(exceptionCode.getResponse()+"");
				}
			} else {
				exceptionCode.setErrorCode(Constants.ERRONEOUS_OPERATION);
				exceptionCode.setErrorMsg("No valid data found in request");
			}
			if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				exceptionCode = widgetDesignWb.modifyRecord(vObject);
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
	
	@RequestMapping(path = "/getQueryMetadata", method = RequestMethod.POST)
	@ApiOperation(value = "Get metadata of query chosen", notes = "Get metadata of query chosen like column listing", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getQueryMetaDataForDesign(@RequestBody VcForQueryReportFieldsWrapperVb vObject) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			VcForQueryReportVb reportVb = vObject.getMainModel();
			exceptionCode = designAnalysisWb.getReportDetailFromReportDefs(reportVb);
			VcForQueryReportFieldsWrapperVb wrapperVb = (VcForQueryReportFieldsWrapperVb) exceptionCode.getResponse();
			if(exceptionCode.getErrorCode()==Constants.SUCCESSFUL_OPERATION) {
				wrapperVb.setHashArray(vObject.getHashArray());
				wrapperVb.setHashValueArray(vObject.getHashValueArray());
			}
			boolean isTableAvailable = false;
			if (ValidationUtil.isValid(vObject.getDataViewName())) {
				exceptionCode = widgetDesignWb.updateWidgetCreationStagingTable(vObject.getDataViewName());
				if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					isTableAvailable = true;
				}
			}

			if (!isTableAvailable) {
				Set<String> uniqueTableIdSet = new HashSet<String>();

				for (VcForQueryReportFieldsVb rCReportFieldsVb : wrapperVb.getReportFields()) {
					if (ValidationUtil.isValid(rCReportFieldsVb.getTabelId())) {
						uniqueTableIdSet.add(rCReportFieldsVb.getTabelId());
					}
				}

				wrapperVb.getMainModel().setBaseTableId(designAnalysisWb.returnBaseTableId(wrapperVb.getMainModel().getCatalogId()));
				exceptionCode = designAnalysisWb.executeCatalogQuery(wrapperVb,uniqueTableIdSet.stream().collect(Collectors.toList()), false, false);
				if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					exceptionCode.setOtherInfo(wrapperVb);
					jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, null, exceptionCode.getResponse(), exceptionCode.getOtherInfo());
				} else {
					jsonExceptionCode = new JSONExceptionCode(Constants.ERRONEOUS_OPERATION, exceptionCode.getErrorMsg(), exceptionCode.getOtherInfo());
				}
			} else {
				exceptionCode.setOtherInfo(wrapperVb);
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, null, vObject.getDataViewName(), exceptionCode.getOtherInfo());
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
	@ApiOperation(value = "Get all widgets", notes = "Get all widgets for listing", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getAll() {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			exceptionCode = widgetDesignWb.getAllWidgetsList();
			if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, null, exceptionCode.getResponse(), exceptionCode.getOtherInfo()); 
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
	@ApiOperation(value = "Get widget based on widget ID", notes = "Get widget based on widget ID from widget table", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> get(@RequestBody WidgetDesignVb widgetDesignVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			exceptionCode = widgetDesignWb.getWidget(widgetDesignVb);
			if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				widgetDesignVb = (WidgetDesignVb) exceptionCode.getResponse();
				exceptionCode = CommonUtils.XmlToJson(widgetDesignVb.getWidgetContext());
				if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					widgetDesignVb.setWidgetContextJson(exceptionCode.getResponse()+"");
				}
				exceptionCode = CommonUtils.XmlToJson(widgetDesignVb.getFilterContext());
				if(exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
					widgetDesignVb.setFilterContextJson(exceptionCode.getResponse()+"");
				}
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, null, widgetDesignVb, exceptionCode.getOtherInfo()); 
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
	
	@RequestMapping(path = "/saveLevelOfDisplay", method = RequestMethod.POST)
	@ApiOperation(value = "Widget - Level Of Display", notes = "save level of display for widget", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> levelOfDisplay(@RequestBody WidgetLODWrapperVb widgetDesignLODVb) {
		JSONExceptionCode jsonExceptionCode = null;
		try {
			ExceptionCode exceptionCode = widgetDesignWb.doInsertRecordForAccessControl(widgetDesignLODVb, true);

			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Access Control - Successful",
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
	
	

	@RequestMapping(path = "/getLevelOfDisplay", method = RequestMethod.POST)
	@ApiOperation(value = "Retrive widget Data - Level Of Display", notes = "Get level of display for widget", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> getLevelOfDisplay(@RequestBody WidgetLODWrapperVb widgetLobVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			exceptionCode = widgetDesignWb.getLODForWidget(widgetLobVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Share Successful",
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
	

	
	@RequestMapping(path = "/deleteWidgetData", method = RequestMethod.POST)
	@ApiOperation(value = "Delete widget Data ", notes = "Delete widget Data ", response = ResponseEntity.class)
	public ResponseEntity<JSONExceptionCode> deleteWidgetData(@RequestBody WidgetDesignVb widgetDesignVb) {
		JSONExceptionCode jsonExceptionCode = null;
		ExceptionCode exceptionCode = new ExceptionCode();
		try {
			exceptionCode = widgetDesignWb.deleteWidgetData(widgetDesignVb);
			if (exceptionCode.getErrorCode() == Constants.SUCCESSFUL_OPERATION) {
				jsonExceptionCode = new JSONExceptionCode(Constants.SUCCESSFUL_OPERATION, "Delete Successful",
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
	
}