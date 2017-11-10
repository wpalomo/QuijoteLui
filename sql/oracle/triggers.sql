/*
Disparador para la secuencia de la tabla ELE_DOCUMENTOS_ELECTRONICOS
*/

CREATE OR REPLACE TRIGGER TR_ELE_DOCUMENTOS_ON_IN
BEFORE INSERT ON ELE_DOCUMENTOS_ELECTRONICOS
for each row
BEGIN
  select SEQ_ELE_DOCUMENTOS.NEXTVAL
  into :new.id
  from dual;
END;