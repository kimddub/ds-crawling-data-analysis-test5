DROP DATABASE IF EXISTS ds5;

CREATE DATABASE ds5;

USE ds5;


	
# drop table `site`;

CREATE TABLE `site`(
	`code` INT(10) AUTO_INCREMENT PRIMARY KEY,
	site VARCHAR(100) NOT NULL
);

SELECT *
FROM `site`;

DESC `site`;

# truncate `site`;

INSERT INTO `site`
VALUES (1,"test"),
	(2,"test");
	
	

# drop table `category`;

CREATE TABLE `category`(
	`code` INT(10) AUTO_INCREMENT PRIMARY KEY,
	sectionId VARCHAR(50) NOT NULL,
	subSectionId VARCHAR(50) NOT NULL,
	section VARCHAR(100) NOT NULL,
	subSection VARCHAR(100) NOT NULL,
	siteCode INT(10) NOT NULL,
	popState TINYINT(1) NOT NULL DEFAULT(0)
);

SELECT *
FROM `category`;

DESC `category`;

# truncate `category`;

INSERT INTO `category`
VALUES (1,"정치","test",1),
	(2,"주요기사","test",1);




# drop table article;

CREATE TABLE article(
	`code` INT(10) AUTO_INCREMENT,
	id INT(20) NOT NULL,
	siteCode INT(10) NOT NULL,
	webPath VARCHAR(100) NOT NULL,
	regDate DATETIME NOT NULL,
	colDate DATETIME NOT NULL,
	PRIMARY KEY(`code`),
	UNIQUE KEY(id,siteCode);
);

SELECT *
FROM article;

DESC article;

# truncate article;

INSERT INTO article (id,siteCode,webPath,regDate,colDate) 
VALUES (5, 1, 'test5', NOW(), NOW()), (2, 1,'test1', NOW(), NOW())
ON DUPLICATE KEY UPDATE
	regDate = VALUES(regDate),
	colDate = VALUES(colDate);

	

# drop table `categorize`;

CREATE TABLE `categorize`(
	`code` INT(10) AUTO_INCREMENT PRIMARY KEY,
	articleCode INT(10) NOT NULL,
	categoryCode INT(10) NOT NULL,
	regDate DATETIME NOT NULL,
	unregDate DATETIME NOT NULL
);


SELECT *
FROM `categorize`;

DESC `categorize`;

# truncate `categorize`;

INSERT INTO `categorize`
VALUES (1,1,1,NOW(),NOW()),
	(2,3,1,NOW(),NOW());



# drop table `keyword`;

CREATE TABLE `keyword`(
	`code` INT(10) AUTO_INCREMENT PRIMARY KEY,
	word VARCHAR(100) NOT NULL,
	articlecode INT(10) NOT NULL
);


SELECT *
FROM `keyword`;

DESC `keyword`;

# truncate `keyword`;

INSERT INTO `keyword`
VALUES (1,"keyword",1),
	(2,"keyword",1),
	(3,"keyword",3);












# 데이터 최근 크롤링 순서로 검색 
SELECT *
FROM article
ORDER BY colDate DESC, regDate DESC;

# 시각화
SELECT A.colDate, S.id AS sourceId, site, category, IFNULL(`count`,0) AS `count`
FROM `source` AS S
RIGHT JOIN (
	SELECT colDate, sourceId, COUNT(*) AS `count`
	FROM article
	GROUP BY sourceId,colDate) AS A
ON S.id = A.sourceId




SELECT *
FROM article AS A1
RIGHT JOIN (
	SELECT colDate
	FROM article
	GROUP BY colDate ) AS A2
ON A1.colDate = A2.colDate
GROUP BY A1.sourceId

SELECT A.colDate, S.*
FROM `source` AS S
RIGHT JOIN (SELECT *
	FROM article
	GROUP BY colDate
	UNION
	SELECT *
	FROM article
	GROUP BY sourceId) AS A
ON S.id = A.sourceId



