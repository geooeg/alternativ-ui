Request the current data from the API:
[REF] http://alter.meteor.com/api/export

[REMARK] Usally it is bad design - atleast in the java world - to name classes like included fields. 
So Class N with field N is a nogo! As a result for now the schema file needed to be changed:
"bounds": {
"N": {
"N_num": 31.77354,
"j_num": 32.07108
},
"j": {
"j_num": 34.66063,
"N_num": 34.76991
}
}

Validate if the JSON response applies to RFC 4627:
[REF] https://jsonformatter.curiousconcept.com

Format it for example online:
[REF] http://www.freeformatter.com/json-formatter.html#ad-output

To generate a json schema from a valid json file use:

[REF] http://jsonschema.net/

# Remark: you might need to remove special characters (spaces,...)


To generate the classes from a jsonschema the following maven plugin is used:
[REF] https://github.com/joelittlejohn/jsonschema2pojo




# Remark: There is actually kind of a standard from google for geo based json
[REF] http://geojson.org/geojson-spec.html