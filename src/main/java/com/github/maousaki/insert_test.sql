


drop trigger Test_Trigger;
drop  table Test;
drop table TestLog;
create table Test(
    U_ID number,
    username varchar2(25)
);

create table TestLog(
  LOG_ID   varchar2(50) primary key ,
  OLD_NAME varchar2(25),
  NEW_NAME VARCHAR2(25)
)/

truncate table Test;


INSERT INTO Test (U_ID, username) --test
VALUES (123, 'saki');
INSERT INTO Test (U_ID, username) VALUES (1, 'to''m');INSERT INTO Test (U_ID, username) VALUES (2,'jerry');INSERT INTO Test (U_ID, username)
VALUES (3,'bob');


create or replace  FUNCTION Test_Function
    RETURN VARCHAR
IS
    x VARCHAR (50);
BEGIN
    x := lower(RAWTOHEX(sys_guid()));
RETURN
substr(x,1,8)||'-'||substr(x,9,4)||'-'||substr(x,13,4)||'-'||substr(x,17,4)||'-'||substr(x,21,12);
END Test_Function;

/

create or replace FUNCTION NUMBER_TO_BIT(V_NUM NUMBER)
    RETURN VARCHAR IS V_RTN VARCHAR(8);--1254546
V_N1  NUMBER;
V_N2  NUMBER;
BEGIN
V_N1 := V_NUM;
LOOP
      V_N2  := MOD(V_N1, 2);
V_N1  := ABS(TRUNC(V_N1 / 2));
V_RTN := TO_CHAR(V_N2) || V_RTN;
EXIT WHEN V_N1 = 0;
END LOOP;
SELECT lpad(V_RTN,8,0)
INTO   V_RTN
FROM dual;
return V_RTN;
end;


/

create or replace package     Package_Test is

  procedure p1(x number);
  -- test
  procedure p2(x number);
  -- 123
 procedure p3(x number);
end Package_Test;


create or replace PACKAGE BODY     Package_Test is
-- PROCEDURE TO INITIALIZE THE TRIGGER NEST LEVEL
PROCEDURE p1(x number)IS
 BEGIN
 update Test set username = 'test1' where U_ID = x;
END;


-- FUNCTION TO RETURN THE TRIGGER NEST LEVEL
PROCEDURE p2(x number)IS
BEGIN
    update Test set username = 'test2' where U_ID = x;
END;

-- PROCEDURE TO INCREASE THE TRIGGER NEST LEVEL
PROCEDURE p3(x number)IS
BEGIN
    update Test set username = 'test3' where U_ID = x;
END;

END Package_Test;

/


create or replace trigger Test_Trigger
    after insert or update or delete
    on Test
    for each row
BEGIN
IF inserting THEN
INSERT INTO TestLog (LOG_ID,new_name) VALUES (Test_Function(), :NEW.username);
ELSIF UPDATING THEN
    INSERT INTO TestLog (LOG_ID,OLD_NAME,new_name) VALUES (Test_Function(),:OLD.username, :NEW.username);
ELSIF DELETING THEN
    INSERT INTO TestLog (LOG_ID,OLD_NAME,new_name) VALUES (Test_Function(),:OLD.username, null);
END IF;
END;

;;

select
    t.U_ID,
    t.username
from Test t
where t.U_ID = '1'
    and t
        .username != 'saki';

create or replace view TestView as
select
    t.U_ID,
    t.username
from Test t;