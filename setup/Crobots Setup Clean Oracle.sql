CREATE TABLE TMP_4 
(
  PKID INTEGER NOT NULL 
, ROBOT1 VARCHAR2(45) NOT NULL 
, ROBOT2 VARCHAR2(45) NOT NULL 
, ROBOT3 VARCHAR2(45) NOT NULL 
, ROBOT4 VARCHAR2(45) NOT NULL 
, STATUS CHAR(1) DEFAULT '0' NOT NULL 
, CONSTRAINT TMP_4_PK PRIMARY KEY 
  (
    PKID 
  )
  ENABLE 
);

CREATE TABLE TMP_3
(
  PKID INTEGER NOT NULL 
, ROBOT1 VARCHAR2(45) NOT NULL 
, ROBOT2 VARCHAR2(45) NOT NULL 
, ROBOT3 VARCHAR2(45) NOT NULL 
, STATUS CHAR(1) DEFAULT '0' NOT NULL 
, CONSTRAINT TMP_3_PK PRIMARY KEY 
  (
    PKID 
  )
  ENABLE 
);

CREATE TABLE TMP_2 
(
  PKID INTEGER NOT NULL 
, ROBOT1 VARCHAR2(45) NOT NULL 
, ROBOT2 VARCHAR2(45) NOT NULL 
, STATUS CHAR(1) DEFAULT '0' NOT NULL 
, CONSTRAINT TMP_2_PK PRIMARY KEY 
  (
    PKID 
  )
  ENABLE
);

CREATE TABLE ROBOTS
  (
    "NAME"   VARCHAR2(12 BYTE) NOT NULL ENABLE,
    "STATUS" SMALLINT DEFAULT 1 NOT NULL ENABLE,
    CONSTRAINT "ROBOTS_PK" PRIMARY KEY ("NAME") USING INDEX  TABLESPACE "TS_CALL_AVOIDANCE_IDX" ENABLE
  )
  TABLESPACE "TS_CALL_AVOIDANCE_DAT" ;
/

CREATE SEQUENCE TMP_IDX MINVALUE 0 MAXVALUE 999999999999999999999999999 INCREMENT BY 1 START WITH 1 CACHE 20 NOORDER NOCYCLE ;

CREATE OR REPLACE
PROCEDURE PRECOVERYF2F(
    ROBOT1 IN VARCHAR2,
    ROBOT2 IN VARCHAR2 )
AS
  PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
  INSERT
  INTO TMP_2
    (
      PKID,
      ROBOT1,
      ROBOT2
    )
    VALUES
    (
      TMP_IDX.nextval,
      ROBOT1,
      ROBOT2
    );
  COMMIT;
END PRECOVERYF2F;
/

CREATE OR REPLACE
PROCEDURE PRECOVERY3vs3(
    ROBOT1 IN VARCHAR2,
    ROBOT2 IN VARCHAR2,
    ROBOT3 IN VARCHAR2)
AS
  PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
  INSERT
  INTO TMP_3
    (
      PKID,
      ROBOT1,
      ROBOT2,
      ROBOT3
    )
    VALUES
    (
      TMP_IDX.nextval,
      ROBOT1,
      ROBOT2,
      ROBOT3
    );
  COMMIT;
END PRECOVERY3vs3;
/

CREATE OR REPLACE
PROCEDURE PRECOVERY4vs4(
    ROBOT1 IN VARCHAR2,
    ROBOT2 IN VARCHAR2,
    ROBOT3 IN VARCHAR2,
    ROBOT4 IN VARCHAR2)
AS
  PRAGMA AUTONOMOUS_TRANSACTION;
BEGIN
  INSERT
  INTO TMP_4
    (
      PKID,
      ROBOT1,
      ROBOT2,
      ROBOT3,
      ROBOT4
    )
    VALUES
    (
      TMP_IDX.nextval,
      ROBOT1,
      ROBOT2,
      ROBOT3,
      ROBOT4
    );
  COMMIT;
END PRECOVERY4vs4;
/

CREATE type tRecord
AS
  OBJECT
  (
    pkid   INTEGER,
    robot1 VARCHAR2(12),
    robot2 VARCHAR2(12),
    robot3 VARCHAR2(12),
    robot4 VARCHAR2(12) ) ;
  /
CREATE OR REPLACE type vrecord
IS
  TABLE OF trecord;
  /
  
CREATE OR REPLACE
PROCEDURE pSelectF2F(
    buffersize IN INTEGER,
    o OUT SYS_REFCURSOR )
AS
  PRAGMA AUTONOMOUS_TRANSACTION;
  v_record vrecord := vRecord();
  CURSOR c1
  IS
    SELECT pkid,
      robot1,
      robot2
    FROM tmp_2
    WHERE rownum <=buffersize FOR UPDATE SKIP LOCKED;
BEGIN
  FOR cur_row IN c1
  LOOP
    v_record.extend;
    v_record(v_record.Last):=tRecord(cur_row.pkid,cur_row.robot1,cur_row.robot2,NULL,NULL);
    DELETE FROM tmp_2 WHERE CURRENT OF c1;
  END LOOP;
  COMMIT;
  OPEN o FOR SELECT pkid,
  robot1,
  robot2 FROM TABLE(CAST(v_record AS vrecord));
END;
/

CREATE OR REPLACE
PROCEDURE pSelect3vs3(
    buffersize IN INTEGER,
    o OUT SYS_REFCURSOR )
AS
  PRAGMA AUTONOMOUS_TRANSACTION;
  v_record vrecord := vRecord();
  CURSOR c1
  IS
    SELECT pkid,
      robot1,
      robot2,
      robot3
    FROM tmp_3
    WHERE rownum <=buffersize FOR UPDATE SKIP LOCKED;
BEGIN
  FOR cur_row IN c1
  LOOP
    v_record.extend;
    v_record(v_record.Last):=tRecord(cur_row.pkid,cur_row.robot1,cur_row.robot2,cur_row.robot3,NULL);
    DELETE FROM tmp_3 WHERE CURRENT OF c1;
  END LOOP;
  COMMIT;
  OPEN o FOR SELECT pkid,
  robot1,
  robot2,
  robot3 FROM TABLE(CAST(v_record AS vrecord));
END;
/

create or replace
PROCEDURE pSelect4vs4(
    buffersize IN INTEGER,
    o OUT SYS_REFCURSOR )
AS
  PRAGMA AUTONOMOUS_TRANSACTION;
  v_record vrecord := vRecord();
  CURSOR c1
  IS
    SELECT pkid,
      robot1,
      robot2,
      robot3,
      robot4
    FROM tmp_4
    WHERE rownum <=buffersize FOR UPDATE SKIP LOCKED;
BEGIN
  FOR cur_row IN c1
  LOOP
    v_record.extend;
    v_record(v_record.Last):=tRecord(cur_row.pkid,cur_row.robot1,cur_row.robot2,cur_row.robot3,cur_row.robot4);
    DELETE FROM tmp_4 WHERE CURRENT OF c1;
  END LOOP;
  COMMIT;
  OPEN o FOR SELECT pkid,
  robot1,
  robot2,
  robot3,
  robot4 FROM TABLE(CAST(v_record AS vrecord));
END;
/

CREATE OR REPLACE
PROCEDURE pCleanUpRobots
AS
BEGIN
  UPDATE robots SET status=9 WHERE status<>9;
END pCleanUpRobots;
/

CREATE OR REPLACE PROCEDURE pInitializeRobot 
(
  PARAM1 IN VARCHAR2  
) AS 
BEGIN
  UPDATE robots SET status=1 WHERE name=PARAM1;
END pInitializeRobot;
/

CREATE OR REPLACE PROCEDURE pSetupF2F
AS
BEGIN
  EXECUTE immediate 'TRUNCATE TABLE TMP_2';
  FOR i IN
  (SELECT t1.name robot1,
    t2.name robot2,
    dbms_random.random n
  FROM
    ( SELECT rownum idx, name, status FROM robots WHERE status=1
    ) t1,
    ( SELECT rownum idx, name FROM robots WHERE status=1
    ) t2
  WHERE t2.idx>t1.idx
  ORDER BY 3
  )
  LOOP
    INSERT
    INTO TMP_2
      (
        PKID,
        ROBOT1,
        ROBOT2
      )
      VALUES
      (
        TMP_IDX.nextval,
        i.robot1,
        i.robot2
      );
  END LOOP;
END pSetupF2F;
/

CREATE OR REPLACE
PROCEDURE pSetup3vs3
AS
BEGIN
  EXECUTE immediate 'TRUNCATE TABLE TMP_3';
  FOR i IN
  (SELECT t1.name robot1,
    t2.name robot2,
    t3.name robot3,
    dbms_random.random n
  FROM
    ( SELECT rownum idx, name, status FROM robots WHERE status=1
    ) t1,
    ( SELECT rownum idx, name FROM robots WHERE status=1
    ) t2,
    ( SELECT rownum idx, name FROM robots WHERE status=1
    ) t3
  WHERE t2.idx>t1.idx
  AND t3.idx  >t2.idx
  ORDER BY 4
  )
  LOOP
    INSERT
    INTO TMP_3
      (
        PKID,
        ROBOT1,
        ROBOT2,
        ROBOT3
      )
      VALUES
      (
        tmp_idx.nextval,
        i.robot1,
        i.robot2,
        i.robot3
      );
  END LOOP;
END pSetup3vs3;
/

CREATE OR REPLACE
PROCEDURE pSetup4vs4
AS
BEGIN
  EXECUTE immediate 'TRUNCATE TABLE TMP_4';
  FOR i IN
  (SELECT t1.name robot1,
    t2.name robot2,
    t3.name robot3,
    t4.name robot4,
    dbms_random.random n
  FROM
    ( SELECT rownum idx, name, status FROM robots WHERE status=1
    ) t1,
    ( SELECT rownum idx, name FROM robots WHERE status=1
    ) t2,
    ( SELECT rownum idx, name FROM robots WHERE status=1
    ) t3,
    ( SELECT rownum idx, name FROM robots WHERE status=1
    ) t4
  WHERE t2.idx>t1.idx
  AND t3.idx  >t2.idx
  AND t4.idx  >t3.idx
  ORDER BY 5
  )
  LOOP
    INSERT
    INTO TMP_4
      (
        PKID,
        ROBOT1,
        ROBOT2,
        ROBOT3,
        ROBOT4
      )
      VALUES
      (
        tmp_idx.nextval,
        i.robot1,
        i.robot2,
        i.robot3,
        i.robot4
      );
  END LOOP;
END pSetup4vs4;
/

--
-- Setup TEST
--
DELETE FROM tmp_2 WHERE robot1<>'test'AND robot2<>'test';
DELETE
FROM tmp_3
WHERE robot1<>'test'
AND robot2  <>'test'
AND robot3  <>'test';
DELETE
FROM tmp_4
WHERE robot1<>'test'
AND robot2  <>'test'
AND robot3  <>'test'
AND robot4  <>'test';