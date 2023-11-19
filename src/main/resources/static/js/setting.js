//页面加载完毕后，调用function函数
//即：当uploadForm表单的submint按钮点击时，执行upload函数
$(function () {
    $("#uploadForm").submit(upload);
});

function upload() {
    //由于需要上传图片格式的文件，$.post()无法满足需求。因此，采用$.ajax()（原始，但是功能更全面）
    //进行完异步上传操作后，不再需要表单的post请求，因此需要在该函数return false
    $.ajax({
        url:"http://upload-z1.qiniup.com",
        method:"post",
        processData:false,//表示不要把表单的内容转换为字符串，因为上传的是图片
        contentType:false,//表示不要让jQuery来主动设置内容类型，而是保留原始格式，后续交给浏览器作插入key来分割二进制内容

        //new FormData是js对象，就是我们需要提交的表单，$("#uploadForm")是jQuery对象，也是Dom对象数组，只需去数组第一位便是js对象
        data:new FormData($("#uploadForm")[0]),
        success:function (data) {//此处为上传完成后运行的函数，data与上一行的data不同，此处的data为响应返回数据,属于Json格式的数据，不需要作Josn解析
            if (data && data.code === 0){
                //更新头像访问路径
                $.post(
                    CONTEXT_PATH + "/user/header/url",
                    {"fileName":$("input[name='key']").val()},//input[]是元素选择器，name=''根据name属性进行选择
                    function (data) {//此处data是String类型，需要先进行Json格式解析
                        data = $.parseJSON(data);
                        if(data.code === 0){
                            window.location.reload();
                        }else{
                            alert(data.msg);
                        }
                    }
                )
            } else{
                alert("上传失败！");
            }
        }
    });
    return false;
}