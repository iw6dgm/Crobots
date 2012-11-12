CREATE TABLE  [4vs4] (
  id int IDENTITY(1,1) NOT NULL,
  robot1 varchar(45) NOT NULL DEFAULT '',
  robot2 varchar(45) NOT NULL DEFAULT '',
  robot3 varchar(45) NOT NULL DEFAULT '',
  robot4 varchar(45) NOT NULL DEFAULT '',
  status integer NOT NULL DEFAULT '0',
  PRIMARY KEY  (id)
);

CREATE INDEX Index_4 ON [4vs4] (status);

CREATE TABLE  [3vs3] (
  id int IDENTITY(1,1) NOT NULL,
  robot1 varchar(45) NOT NULL DEFAULT '',
  robot2 varchar(45) NOT NULL DEFAULT '',
  robot3 varchar(45) NOT NULL DEFAULT '',
  status integer NOT NULL DEFAULT '0',
  PRIMARY KEY  (id)
);

CREATE INDEX Index_3 ON [3vs3] (status);

CREATE TABLE  [f2f] (
  id int IDENTITY(1,1) NOT NULL,
  robot1 varchar(45) NOT NULL DEFAULT '',
  robot2 varchar(45) NOT NULL DEFAULT '',
  status integer NOT NULL DEFAULT 0,
  PRIMARY KEY  (id)
);

CREATE INDEX Index_2 ON [F2F] (status);

CREATE TABLE  [robots] (
  id int IDENTITY(1,1) NOT NULL,
  name varchar(12) NOT NULL,
  status smallint NOT NULL DEFAULT '0',
  PRIMARY KEY  (id)
);

CREATE UNIQUE INDEX [name_idx] ON [robots] 
(
	[name] ASC
);

CREATE TABLE  [koth] (
  id int IDENTITY(1,1) NOT NULL,
  name varchar(12) NOT NULL,
  status smallint NOT NULL DEFAULT '0',
  PRIMARY KEY  (id)
);

CREATE UNIQUE INDEX [name_k_idx] ON [koth] 
(
	[name] ASC
);

USE [Crobots]
GO
/****** Object:  StoredProcedure [dbo].[pSelectF2F]    Script Date: 02/14/2011 15:32:59 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER PROCEDURE [dbo].[pSelectF2F]
	@BufferSize INT
AS
BEGIN
	SET NOCOUNT ON;
	DECLARE @TmpTable TABLE (id INT, robot1 VARCHAR(45), robot2 VARCHAR(45));
	BEGIN TRANSACTION;
	DELETE TOP(@BufferSize) FROM F2F WITH (XLOCK,ROWLOCK) OUTPUT deleted.id,deleted.robot1,deleted.robot2;-- WHERE status=@CurrentID;
	COMMIT TRANSACTION;
	-- COMMIT;
	SELECT id,robot1,robot2 FROM @TmpTable;	
END

USE [Crobots]
GO
/****** Object:  StoredProcedure [dbo].[pSelect3vs3]    Script Date: 02/14/2011 15:46:05 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER PROCEDURE [dbo].[pSelect3vs3] 
	@BufferSize INT
AS
BEGIN
	SET NOCOUNT ON;
	DECLARE @TmpTable TABLE (id INT, robot1 VARCHAR(45), robot2 VARCHAR(45), robot3 VARCHAR(45));
	BEGIN TRANSACTION;
	DELETE TOP(@BufferSize) FROM [3vs3] WITH (XLOCK,ROWLOCK) OUTPUT deleted.id,deleted.robot1,deleted.robot2,deleted.robot3;-- WHERE status=@CurrentID;
	COMMIT TRANSACTION;
	-- COMMIT;
	SELECT id,robot1,robot2,robot3 FROM @TmpTable;	
END

USE [Crobots]
GO
/****** Object:  StoredProcedure [dbo].[pSelect4vs4]    Script Date: 02/14/2011 15:47:56 ******/
SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER PROCEDURE [dbo].[pSelect4vs4]
	@BufferSize INT
AS
BEGIN
	SET NOCOUNT ON;
	DECLARE @TmpTable TABLE (id INT, robot1 VARCHAR(45), robot2 VARCHAR(45), robot3 VARCHAR(45), robot4 VARCHAR(45));
	BEGIN TRANSACTION;
	DELETE TOP(@BufferSize) FROM [4vs4] WITH (XLOCK,ROWLOCK) OUTPUT deleted.id,deleted.robot1,deleted.robot2,deleted.robot3,deleted.robot4;-- WHERE status=@CurrentID;
	COMMIT TRANSACTION;
	-- COMMIT;
	SELECT id,robot1,robot2,robot3,robot4 FROM @TmpTable;	
END

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [pRecoveryF2F]
	@vid INT
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
	BEGIN TRANSACTION;
	UPDATE F2F WITH (XLOCK,ROWLOCK) SET status=0 WHERE id=@vid;
  COMMIT TRANSACTION;
	COMMIT;
END
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [pRecovery3vs3]
	@vid INT
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
	BEGIN TRANSACTION;
	UPDATE [3vs3] WITH (XLOCK,ROWLOCK) SET status=0 WHERE id=@vid;
  COMMIT TRANSACTION;
	COMMIT;
END
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [pRecovery4vs4]
	@vid INT
AS
BEGIN
	-- SET NOCOUNT ON added to prevent extra result sets from
	-- interfering with SELECT statements.
	SET NOCOUNT ON;
	BEGIN TRANSACTION;
	UPDATE [4vs4] WITH (XLOCK,ROWLOCK) SET status=0 WHERE id=@vid;
  COMMIT TRANSACTION;
	COMMIT;
END
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [pCleanUpRobots]
AS
BEGIN
	SET NOCOUNT ON;
	UPDATE robots SET status=9 WHERE status<>9;
	COMMIT;
END
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
CREATE PROCEDURE [pInitializeRobot]
 @robotName VARCHAR(12)
AS
BEGIN
	SET NOCOUNT ON;
	UPDATE robots SET status=1 WHERE name=@robotName;
	COMMIT;
END
GO

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER PROCEDURE [dbo].[pSetupF2F]
AS
BEGIN
	SET NOCOUNT ON;
	TRUNCATE TABLE [f2f];
	INSERT INTO
	[f2f](robot1,robot2)
	SELECT t1.robot AS robot1,
						t2.robot       AS robot2
					  FROM
	(SELECT id idx, name robot FROM robots WHERE status=1) t1,
	(SELECT id idx, name robot FROM robots  WHERE status=1) t2
	WHERE t2.idx>t1.idx
	ORDER BY newid();
	COMMIT;
END

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER PROCEDURE [dbo].[pSetup3VS3]
AS
BEGIN
	SET NOCOUNT ON;
	TRUNCATE TABLE [results_3vs3];
	INSERT INTO [results_3vs3](robot)
	SELECT name AS robot
	FROM [robots]
	WHERE status=2;
	TRUNCATE TABLE [3vs3];
	INSERT INTO
	[3vs3](robot1,robot2,robot3)
	SELECT t1.robot AS robot1,
						t2.robot       AS robot2,
						t3.robot       AS robot3
					  FROM
	(SELECT id idx, name robot FROM robots WHERE status=1) t1,
	(SELECT id idx, name robot FROM robots  WHERE status=1) t2,
	(SELECT id idx, name robot FROM robots  WHERE status=1) t3
	WHERE t2.idx>t1.idx
	AND t3.idx  >t2.idx
	ORDER BY newid();
	COMMIT;
END

SET ANSI_NULLS ON
GO
SET QUOTED_IDENTIFIER ON
GO
ALTER PROCEDURE [dbo].[pSetup4VS4]
AS
BEGIN
	SET NOCOUNT ON;
	TRUNCATE TABLE [results_4vs4];
	INSERT INTO [results_4vs4](robot)
	SELECT name AS robot
	FROM [robots]
	WHERE status=2;
	TRUNCATE TABLE [4vs4];
	INSERT INTO
	[4vs4](robot1,robot2,robot3,robot4)
	SELECT t1.robot AS robot1,
						t2.robot       AS robot2,
						t3.robot       AS robot3,
						t4.robot       AS robot4
					  FROM
	(SELECT id idx, name robot FROM robots WHERE status=1) t1,
	(SELECT id idx, name robot FROM robots  WHERE status=1) t2,
	(SELECT id idx, name robot FROM robots  WHERE status=1) t3,
	(SELECT id idx, name robot FROM robots  WHERE status=1) t4
	WHERE t2.idx>t1.idx
	AND t3.idx  >t2.idx
	AND t4.idx  >t3.idx
	ORDER BY newid();
	COMMIT;
END

INSERT INTO
[f2f](robot1,robot2)
SELECT t1.robot AS robot1,
                    t2.robot       AS robot2
                  FROM
(SELECT id idx, name robot FROM robots WHERE status=1) t1,
(SELECT id idx, name robot FROM robots  WHERE status=1) t2
WHERE t2.idx>t1.idx
ORDER BY newid();

INSERT INTO
[3vs3](robot1,robot2,robot3)
SELECT t1.robot AS robot1,
                    t2.robot       AS robot2,
                    t3.robot       AS robot3
                  FROM
(SELECT id idx, name robot FROM robots WHERE status=1) t1,
(SELECT id idx, name robot FROM robots  WHERE status=1) t2,
(SELECT id idx, name robot FROM robots  WHERE status=1) t3
WHERE t2.idx>t1.idx
AND t3.idx  >t2.idx
ORDER BY newid();

INSERT INTO
[4vs4](robot1,robot2,robot3,robot4)
SELECT t1.robot AS robot1,
                    t2.robot       AS robot2,
                    t3.robot       AS robot3,
					t4.robot       AS robot4
                  FROM
(SELECT id idx, name robot FROM robots WHERE status=1) t1,
(SELECT id idx, name robot FROM robots  WHERE status=1) t2,
(SELECT id idx, name robot FROM robots  WHERE status=1) t3,
(SELECT id idx, name robot FROM robots  WHERE status=1) t4
WHERE t2.idx>t1.idx
AND t3.idx  >t2.idx
AND t4.idx  >t3.idx
ORDER BY newid();

--
-- KOTH
--

--
-- All
--
INSERT INTO
[f2f](robot1,robot2)
SELECT t1.robot AS robot1,
                    t2.robot       AS robot2
                  FROM
(SELECT id idx, name robot FROM koth WHERE status=1) t1,
(SELECT id idx, name robot FROM koth WHERE status=1) t2
WHERE t2.idx>t1.idx
ORDER BY newid();

--
-- Selected
--
INSERT INTO
[f2f](robot1,robot2)
SELECT t1.robot AS robot1,
                    t2.robot       AS robot2
                  FROM
(SELECT name robot FROM koth WHERE status=2) t1,
(SELECT name robot FROM koth) t2
WHERE t1.robot<>t2.robot

--
-- Single
--
INSERT INTO
[f2f](robot1,robot2)
SELECT 'jedi10' AS robot1,
                    name       AS robot2
                  FROM koth
WHERE name<>'jedi10'
