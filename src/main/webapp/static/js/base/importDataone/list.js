/**
 * 模板一数据js
 */

$(function () {
	initialPage();
	getGrid();
});


/**
 *
 */
/*function uplod(){
    var formData = new FormData();
    formData.append('file', $('#file')[0].files[0]);
    console.log("=========="+formData)
}*/
$(function () {
    $("#upload").click(function () {
        alert(123);
        var formData = new FormData();
        formData.append('file', $('#file')[0].files[0]);
        console.log("==========" + formData)
        $.ajax({
             url: "/sys/importDataone/getUploadData",
             type: "POST",
             data: formData,
             /**
              *必须false才会自动加上正确的Content-Type
              */
             contentType: false,
             /**
              * 必须false才会避开jQuery对formdaa的默认处理
              * XMLHttpRequest会对formdata进行正确的处理
              */
             processData: false,
             success: function (data) {
                 alert("success");

             },
             error: function () {
                 alert("上传失败！");

             }
         });
    })

});
function initialPage() {
	$(window).resize(function() {
		$('#dataGrid').bootstrapTable('resetView', {height: $(window).height()-56});
	});
}

function getGrid() {
	$('#dataGrid').bootstrapTableEx({
		url: '../../sys/importDataone/list?_' + $.now(),
		height: $(window).height()-56,
		queryParams: function(params){
			params.name = vm.keyword;
			return params;
		},
		columns: [
			{checkbox: true},
            {field : "importDataoneId", title : "导入模板一表主键", width : "100px"},
            {field : "feeArea", title : "资费区（一区-九区）", width : "100px"},
            {field : "accessToTheState", title : "通达国家（地区，每个地区逗号与逗号隔开）", width : "100px"},
            {field : "price", title : "价格", width : "100px"},
            {field : "goodsType", title : "货物类型（1.文件，2.物品）", width : "100px"},
            {field : "firstPriority", title : "首重（统一为kg）", width : "100px"},
            {field : "referenceTime", title : "参考时限 （工作日）", width : "100px"},
            {field : "maximumWeight", title : "最高限重（kg）", width : "100px"},
            {field : "maximumSizeLimit", title : "最大尺寸限制（m）", width : "100px"},
            {field : "compensationStandard", title : "补偿标准", width : "100px"},
            {field : "remarks", title : "备注", width : "100px"},
            {field : "lifting", title : "起重（单位g）", width : "100px"},
            {field : "gmtCreate", title : "创建时间", width : "100px"},
            {field : "gmtModified", title : "修改时间", width : "100px"},
            {title : "操作", formatter : function(value, row, index) {
                    var _html = '';
                    if (hasPermission('sys:importDataone:edit')) {
                        _html += '<a href="javascript:;" onclick="vm.edit(\''+row.importDataoneId+'\')" title="编辑"><i class="fa fa-pencil"></i></a>';
                    }
                    if (hasPermission('sys:importDataone:remove')) {
                        _html += '<a href="javascript:;" onclick="vm.remove(false,\''+row.importDataoneId+'\')" title="删除"><i class="fa fa-trash-o"></i></a>';
                    }
                    return _html;
                }
            }
		]
	})
}

var vm = new Vue({
	el:'#dpLTE',
	data: {
		keyword: null,
        rfCardNumber:''
	},
	methods : {
		load: function() {
			$('#dataGrid').bootstrapTable('refresh');
		},
		save: function() {
			dialogOpen({
				title: '新增模板一数据',
				url: 'base/importDataone/add.html?_' + $.now(),
				width: '420px',
				height: '350px',
				yes : function(iframeId) {
					top.frames[iframeId].vm.acceptClick();
				},
			});
		},
		edit: function(importDataoneId) {
            dialogOpen({
                title: '编辑模板一数据',
                url: 'base/importDataone/edit.html?_' + $.now(),
                width: '420px',
                height: '350px',
                success: function(iframeId){
                    top.frames[iframeId].vm.importDataone.importDataoneId = importDataoneId;
                    top.frames[iframeId].vm.setForm();
                },
                yes: function(iframeId){
                    top.frames[iframeId].vm.acceptClick();
                }
            });
        },
        remove: function(batch, importDataoneId) {
            var ids = [];
            if (batch) {
                var ck = $('#dataGrid').bootstrapTable('getSelections');
                if (!checkedArray(ck)) {
                    return false;
                }
                $.each(ck, function(idx, item){
                    ids[idx] = item.importDataoneId;
                });
            } else {
                ids.push(importDataoneId);
            }
            $.RemoveForm({
                url: '../../sys/importDataone/remove?_' + $.now(),
                param: ids,
                success: function(data) {
                    vm.load();
                }
            });
        }
	}
})