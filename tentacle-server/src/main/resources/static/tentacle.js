window.Tentacle = {
    imageData : null,
    canvas : null,
    totalTransfered : 0,
    keyboard : [],
    mouse : [],
    connection : null,
    state : 'init',
    frames : [],

    // 初始化
    init : function()
    {
        this._connect();
        this._bindEvents();
        this._showLoginDialog();
    },
    // 身份验证
    login : function()
    {

    },
    // 断开连接
    disconnect : function()
    {
        this.connection.close();
    },
    _send : function(cmd)
    {
        this.connection.send(JSON.stringify(cmd));
    },
    // 事件绑定
    _bindEvents : function()
    {

    },
    _connect : function()
    {
        this.connection = new WebSocket('ws://' + location.host + '/tentacle/desktop/ws');
        this.connection.onopen = this._onopen;
        this.connection.onmessage = this._onmessage;
        this.connection.onclose = this._onclose;
        this.connection.onerror = this._onerror;
        this.connection.binaryType = 'arraybuffer';
    },
    _onopen : function() { },
    _onmessage : function(resp)
    {
        if (resp.data instanceof ArrayBuffer)
        {
            var response = eval(resp.data);
            if ('login' == resp.action)
            {
                if (resp.result == 'success') this.animateCss('bounceOut');
                else $('.x-message').text(resp.result);
            }
            else if ('request-control' == resp.action)
            {
                if (resp.result != 'success') this.showMessage(resp.result);
            }
            else if ('read-clipboard' == resp.action)
            {

            }
            else if ('write-clipboard' == resp.action)
            {

            }
        }
        else
        {
            var packet = new Uint8Array(resp.data);
            this.state = 'connected';
            // TODO: 连接响应包处理
            // 截屏画面包处理
            this.frames.push(packet);
        }
    },
    _onclose : function() { },
    _onerror : function() { },
    showMessage : function(text)
    {
        var timeout = 2000;
        var greeting = $('<div class="x-message-box">' + text + '</div>');
        $(document.body).append(greeting);
        greeting.css({ top : (document.body.scrollTop + window.innerHeight - 100) + 'px' }).show().animateCss('bounceIn', function()
        {
            setTimeout(function()
            {
                greeting.remove();
            }, timeout);
        });
    },
    clipboard : {
        fromRemote : function()
        {

        },
        toRemote : function(text)
        {

        }
    }
};