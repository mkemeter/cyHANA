SELECT_CURRENT_SCHEMA=\
SELECT CURRENT_SCHEMA FROM DUMMY

LIST_GRAPH_WORKSPACES=\
SELECT SCHEMA_NAME, WORKSPACE_NAME FROM GRAPH_WORKSPACES WHERE IS_VALID = 'TRUE'

LOAD_WORKSPACE_METADATA_HANA_CLOUD=\
SELECT ENTITY_TYPE, ENTITY_ROLE, ENTITY_SCHEMA_NAME, ENTITY_TABLE_NAME, ENTITY_COLUMN_NAME \
FROM GRAPH_WORKSPACE_COLUMNS WHERE SCHEMA_NAME = ? AND WORKSPACE_NAME = ?

LOAD_WORKSPACE_METADATA_HANA_ONPREM=\
SELECT ENTITY_TYPE, ENTITY_ROLE, ENTITY_SCHEMA_NAME, ENTITY_TABLE_NAME, ENTITY_COLUMN_NAME FROM \
( \
	SELECT SCHEMA_NAME, WORKSPACE_NAME, 'EDGE' ENTITY_TYPE, 'KEY' ENTITY_ROLE, EDGE_SCHEMA_NAME ENTITY_SCHEMA_NAME, EDGE_TABLE_NAME ENTITY_TABLE_NAME, EDGE_KEY_COLUMN_NAME ENTITY_COLUMN_NAME, IS_VALID FROM GRAPH_WORKSPACES \
	UNION \
	SELECT SCHEMA_NAME, WORKSPACE_NAME, 'EDGE' ENTITY_TYPE, 'SOURCE' ENTITY_ROLE, EDGE_SCHEMA_NAME ENTITY_SCHEMA_NAME, EDGE_TABLE_NAME ENTITY_TABLE_NAME, EDGE_SOURCE_COLUMN_NAME ENTITY_COLUMN_NAME, IS_VALID FROM GRAPH_WORKSPACES \
	UNION \
	SELECT SCHEMA_NAME, WORKSPACE_NAME, 'EDGE' ENTITY_TYPE, 'TARGET' ENTITY_ROLE, EDGE_SCHEMA_NAME ENTITY_SCHEMA_NAME, EDGE_TABLE_NAME ENTITY_TABLE_NAME, EDGE_TARGET_COLUMN_NAME ENTITY_COLUMN_NAME, IS_VALID FROM GRAPH_WORKSPACES \
	UNION \
	SELECT SCHEMA_NAME, WORKSPACE_NAME, 'VERTEX' ENTITY_TYPE, 'KEY' ENTITY_ROLE, VERTEX_SCHEMA_NAME ENTITY_SCHEMA_NAME, VERTEX_TABLE_NAME ENTITY_TABLE_NAME, VERTEX_KEY_COLUMN_NAME ENTITY_COLUMN_NAME, IS_VALID FROM GRAPH_WORKSPACES \
	UNION \
	SELECT WS.SCHEMA_NAME, WS.WORKSPACE_NAME, 'EDGE' ENTITY_TYPE, NULL ENTITY_ROLE, WS.EDGE_SCHEMA_NAME ENTITY_SCHEMA_NAME, WS.EDGE_TABLE_NAME ENTITY_TABLE_NAME, C.COLUMN_NAME ENTITY_COLUMN_NAME, WS.IS_VALID \
	FROM GRAPH_WORKSPACES WS \
	LEFT JOIN TABLE_COLUMNS C \
		ON WS.EDGE_SCHEMA_NAME = C.SCHEMA_NAME AND WS.EDGE_TABLE_NAME = C.TABLE_NAME \
	WHERE C.COLUMN_NAME NOT IN (WS.EDGE_KEY_COLUMN_NAME, WS.EDGE_SOURCE_COLUMN_NAME, WS.EDGE_TARGET_COLUMN_NAME) \
	UNION \
	SELECT WS.SCHEMA_NAME, WS.WORKSPACE_NAME, 'VERTEX' ENTITY_TYPE, NULL ENTITY_ROLE, WS.VERTEX_SCHEMA_NAME ENTITY_SCHEMA_NAME, WS.VERTEX_TABLE_NAME ENTITY_TABLE_NAME, C.COLUMN_NAME ENTITY_COLUMN_NAME, WS.IS_VALID \
	FROM GRAPH_WORKSPACES WS \
	LEFT JOIN TABLE_COLUMNS C \
		ON WS.VERTEX_SCHEMA_NAME = C.SCHEMA_NAME AND WS.VERTEX_TABLE_NAME = C.TABLE_NAME \
	WHERE C.COLUMN_NAME NOT IN (WS.VERTEX_KEY_COLUMN_NAME) \
) WHERE SCHEMA_NAME = ? AND WORKSPACE_NAME = ? AND IS_VALID = 'TRUE'

LOAD_NETWORK_NODES=\
SELECT "%s" %s FROM "%s"."%s"

LOAD_NETWORK_EDGES=\
SELECT "%s", "%s", "%s" %s FROM "%s"."%s"

GET_BUILD=\
SELECT VALUE FROM M_HOST_INFORMATION WHERE KEY='build_branch'
