--------------------------------------------------------
--  DDL for Table ELE_TRUNCADOS
--------------------------------------------------------

  CREATE TABLE "ELE_TRUNCADOS" 
   (	"ID" NUMBER, 
	"CODIGO" VARCHAR2(20 BYTE), 
	"NUMERO" VARCHAR2(20 BYTE), 
	"FECHA" DATE, 
	"OBSERVACION" VARCHAR2(4000 BYTE)
   )   TABLESPACE "USERS" ;
--------------------------------------------------------
--  DDL for Index PK_ELE_TRUNCADOS
--------------------------------------------------------

  CREATE UNIQUE INDEX "PK_ELE_TRUNCADOS" ON "ELE_TRUNCADOS" ("ID") 
   TABLESPACE "USERS" ;
--------------------------------------------------------
--  Constraints for Table ELE_TRUNCADOS
--------------------------------------------------------

  ALTER TABLE "ELE_TRUNCADOS" MODIFY ("ID" NOT NULL ENABLE);
  ALTER TABLE "ELE_TRUNCADOS" ADD CONSTRAINT "PK_ELE_TRUNCADOS" PRIMARY KEY ("ID")
  USING INDEX TABLESPACE "USERS"  ENABLE;
