package com.centit.workflow.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.centit.framework.common.JsonResultUtils;
import com.centit.framework.common.ResponseMapData;
import com.centit.framework.core.controller.BaseController;
import com.centit.framework.core.controller.WrapUpResponseBody;
import com.centit.framework.core.dao.PageQueryResult;
import com.centit.support.database.utils.PageDesc;
import com.centit.workflow.po.FlowOptInfo;
import com.centit.workflow.po.FlowOptPage;
import com.centit.workflow.service.FlowOptService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 2018年9月12日10:46:03
 */
@Controller
@Api(value = "流程业务",
    tags = "流程业务")
@RequestMapping("/flow/opt")
public class FlowOptController extends BaseController {

    private ResponseMapData resData=new ResponseMapData();

    @Autowired
    private FlowOptService wfOptService;

    //工作流--流程定义--业务模块取值
    @ApiOperation(value = "获取业务列表", notes = "获取业务列表")
    @WrapUpResponseBody
    @RequestMapping(value="flow_listFlowOptInfo" ,method = RequestMethod.GET)
    public List<FlowOptInfo> getListFlowOptInfo(){
         return wfOptService.getListOptInfo();
    }
    @ApiOperation(value = "分页获取业务列表", notes = "分页获取业务列表")
    @WrapUpResponseBody
    @RequestMapping(value="/listOptInfo",method = RequestMethod.GET)
    public PageQueryResult listOptInfo(PageDesc pageDesc, HttpServletRequest request){
        Map<String, Object> filterMap = BaseController.collectRequestParameters(request);
        JSONArray objList = wfOptService.listOptInfo(filterMap,pageDesc);
        return PageQueryResult.createResult(objList,pageDesc);
    }
    @ApiOperation(value = "获取指定业务", notes = "获取指定业务")
    @WrapUpResponseBody
    @RequestMapping(value="/getOptById",method = RequestMethod.GET)
    public FlowOptInfo getOptById(String optId){
        return wfOptService.getOptById(optId);
    }
    @ApiOperation(value = "删除业务", notes = "删除业务")
    @WrapUpResponseBody
    @RequestMapping(value="/deleteOptInfoById",method = RequestMethod.DELETE)
    public void deleteOptInfoById(String optId){
        wfOptService.deleteOptInfoById(optId);
    }
    @ApiOperation(value = "保存业务", notes = "保存业务")
    @WrapUpResponseBody
    @RequestMapping(value="/saveOpt",method = RequestMethod.POST)
    public void saveOpt(@RequestBody FlowOptInfo FlowOptInfo){
        wfOptService.saveOpt(FlowOptInfo);
    }

    //根据optId获取表wf_optdef中数据，分页功能没有加！
    @ApiOperation(value = "根据optId获取流程页面", notes = "根据optId获取流程页面")
    @WrapUpResponseBody
    @RequestMapping(value="/getListOptDefById",method = RequestMethod.GET)
    public PageQueryResult getListOptDefById(String optId, PageDesc pageDesc, HttpServletRequest request, HttpServletResponse response){
        Map<String, Object> filterMap = BaseController.collectRequestParameters(request);

//        List<FlowOptDef> objList = wfOptService.getListOptDefById(optId, filterMap,pageDesc);
        List<FlowOptPage> objList = wfOptService.ListOptDef(filterMap,pageDesc);
       return PageQueryResult.createResult(objList,pageDesc);
    }
    @ApiOperation(value = "根据optCode获取流程页面", notes = "根据optCode获取流程页面")
    @WrapUpResponseBody
    @RequestMapping(value="/getOptDefByCode",method = RequestMethod.GET)
    public FlowOptPage getOptDefByCode(String optCode){
        return wfOptService.getOptDefByCode(optCode);
    }
    @ApiOperation(value = "保存流程页面", notes = "保存流程页面")
    @WrapUpResponseBody
    @RequestMapping(value="/saveOptDef",method = RequestMethod.POST)
    public void saveOptDef(@RequestBody FlowOptPage FlowOptDef){
        wfOptService.saveOptDef(FlowOptDef);
    }
    @ApiOperation(value = "删除流程页面", notes = "删除流程页面")
    @RequestMapping(value="/deleteOptDefByCode",method = RequestMethod.DELETE)
    @WrapUpResponseBody
    public void deleteOptDefByCode(String optCode){
        wfOptService.deleteOptDefByCode(optCode);
    }

    @RequestMapping(
            value = {"/createOptInfo"},
            method = {RequestMethod.GET}
    )
    public void getNextOptInfoId(HttpServletResponse response) {
        String optInfoId = wfOptService.getOptInfoSequenceId();
        FlowOptInfo copy = new FlowOptInfo();
        copy.setOptId(optInfoId);
        copy.setUpdateDate(new Date());
        JsonResultUtils.writeSingleDataJson(copy, response);
    }

    @RequestMapping("createOptDef")
    public void getNextOptDefId(String optId, HttpServletResponse response) {
        String optDefCode = wfOptService.getOptDefSequenceId();
        FlowOptPage copy = new FlowOptPage();
        copy.setOptCode(optDefCode);
        copy.setOptId(optId);
        copy.setUpdateDate(new Date());
        JsonResultUtils.writeSingleDataJson(copy, response);
    }

    @RequestMapping(
            value = {"/{optId}"},
            method = {RequestMethod.GET}
    )
    public void getOptInfoById(@PathVariable String optId, HttpServletResponse response) {
        FlowOptInfo FlowOptInfo = this.wfOptService.getFlowOptInfoById(optId);
        JsonResultUtils.writeSingleDataJson(FlowOptInfo, response);
    }
    @ApiOperation(value = "批量保存流程页面", notes = "批量保存流程页面")
    @WrapUpResponseBody
    @RequestMapping(value="/saveOptDefs",method = RequestMethod.POST)
    public void saveOptDefs(@RequestBody JSONObject paramData){
        JSONArray optDefs = paramData.getJSONArray("optDefs");
        for (int i=0; i<optDefs.size(); i++){
            FlowOptPage flowOptDef = optDefs.getObject(i, FlowOptPage.class);
            wfOptService.saveOptDef(flowOptDef);
        }
    }
}
