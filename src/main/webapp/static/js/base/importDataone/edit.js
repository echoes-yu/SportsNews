/**
 * 编辑-模板一数据js
 */
var vm = new Vue({
	el:'#dpLTE',
	data: {
		importDataone: {
			
importDataoneId: 0
		}
	},
	methods : {
		setForm: function() {
			$.SetForm({
				url: '../../sys/importDataone/info?_' + $.now(),
		    	param: vm.importDataone.
importDataoneId,
		    	success: function(data) {
		    		vm.importDataone = data;
		    	}
			});
		},
		acceptClick: function() {
			if (!$('#form').Validform()) {
		        return false;
		    }
		    $.ConfirmForm({
		    	url: '../../sys/importDataone/update?_' + $.now(),
		    	param: vm.importDataone,
		    	success: function(data) {
		    		$.currentIframe().vm.load();
		    	}
		    });
		}
	}
})