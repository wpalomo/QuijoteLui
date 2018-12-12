--------------------------------------------------------
--  DDL for Table ELE_DOCUMENTOS_ELECTRONICOS
--------------------------------------------------------

  CREATE TABLE "ELE_DOCUMENTOS_ELECTRONICOS" 
   (	"ID" NUMBER, 
	"CODIGO" VARCHAR2(20 BYTE), 
	"NUMERO" VARCHAR2(20 BYTE), 
	"NUMERO_AUTORIZACION" VARCHAR2(100 BYTE), 
	"FECHA_AUTORIZACION" DATE, 
	"OBSERVACION" VARCHAR2(4000 BYTE), 
	"ESTADO" VARCHAR2(20 BYTE)
   ) 
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  DDL for Index PK_ELE_DOCUMENTO_ELECTRONICO
--------------------------------------------------------

  CREATE UNIQUE INDEX "PK_ELE_DOCUMENTO_ELECTRONICO" ON "ELE_DOCUMENTOS_ELECTRONICOS" ("ID") 
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  DDL for Index UK_ELE_DOCUMENTOS
--------------------------------------------------------

  CREATE UNIQUE INDEX "UK_ELE_DOCUMENTOS" ON "ELE_DOCUMENTOS_ELECTRONICOS" ("CODIGO", "NUMERO") 
  
  TABLESPACE "USERS" ;
--------------------------------------------------------
--  DDL for Trigger TR_ELE_DOCUMENTOS_ON_IN
--------------------------------------------------------

--------------------------------------------------------
--  Constraints for Table ELE_DOCUMENTOS_ELECTRONICOS
--------------------------------------------------------

   ALTER TABLE "ELE_DOCUMENTOS_ELECTRONICOS" MODIFY ("NUMERO" NOT NULL ENABLE);
  ALTER TABLE "ELE_DOCUMENTOS_ELECTRONICOS" MODIFY ("CODIGO" NOT NULL ENABLE);
  ALTER TABLE "ELE_DOCUMENTOS_ELECTRONICOS" MODIFY ("ID" NOT NULL ENABLE);
