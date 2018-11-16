USE [quijotelui]
GO

/****** Object:  View [dbo].[v_ele_informaciones]    Script Date: 15/11/2018 21:01:12 ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO


create VIEW [dbo].[v_ele_informaciones]
AS
with informaciones as (
SELECT ROW_NUMBER() OVER(ORDER BY entidad ASC) as id, entidad as documento, 
replace(replace(replace(nombre, '$', ''), '!', ''), '=', '') as nombre, 
replace(replace(replace(valor, '$', ''), '!', ''), '=', '') as valor
FROM
(
  SELECT entidad,  
  CONVERT(varchar(100), direccion+'$') as p1,
  CONVERT(varchar(100), fono+'=') as p2,
  CONVERT(varchar(100), lower(email)+'!') as p3,
  CONVERT(varchar(10), 'Dirección'+'$') as n1,
  CONVERT(varchar(10), 'Teléfono'+'=') as n2,
  CONVERT(varchar(10), 'Email'+'!') as n3
  FROM BDQualityv.dbo.Entidad
  where Empresa = 'Activefun'
  and TipoEntidad = 'CLIENTE'
) AS cp
UNPIVOT 
(
  valor FOR valores IN ( p1, p2, p3)  
) AS up
UNPIVOT 
(
  nombre FOR nombres IN ( n1, n2, n3)  
) AS up
where RIGHT(nombre,1) = RIGHT(valor,1)
) select id, documento, nombre, valor from informaciones
where valor <> ''


GO


