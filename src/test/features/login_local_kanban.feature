#language:zh-CN
功能: 登录

  背景:
    * BASE http://localhost:8080
    * HEADER
      """
      bar = foo
      fuck = suck
      """
    * COOKIE
      """
      BB=FF
      """
    * POST FORM /login
      """
      username = zxgjava@163.com
      password = 1
      """
    * STATUS 200
    #* JSONPATH result ok

  场景: 测试
    * GET /api/v1/users/auth
    * STATUS 200
    * JSONPATH result 0
    
