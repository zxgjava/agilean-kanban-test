# Rest-Test
A collection of cucumber keywords to drive rest api test.

# Example
	Feature:get with session.
	
      Background:
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
        * POST FORM /j_spring_security_check
          """
          username = demo
          password = demo
          """
        * STATUS 200
        * JSONPATH result ok
	
      Scenario: get user list
        * GET /users
        * STATUS 200
        * JSONPATH [0].user alex