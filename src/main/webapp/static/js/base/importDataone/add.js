/**
 * 新增-模板一数据js
 */
var vm = new Vue({
	el:'#dpLTE',
	data: {
		importDataone: {
			
importDataoneId: 0
		}
	},
	methods : {
		acceptClick: function() {
			if (!$('#form').Validform()) {
		        return false;
		    }
		    $.SaveForm({
		    	url: '../../sys/importDataone/save?_' + $.now(),
		    	param: vm.importDataone,
		    	success: function(data) {
		    		$.currentIframe().vm.load();
		    	}
		    });
		}
	}
})
