package com.jhl.mds.services.mysql;

import com.jhl.mds.dto.MySQLFieldDTO;
import org.springframework.stereotype.Service;

// ------- Numeric --------
// TINYINT - A 1-byte integer, signed range is -128 to 127, unsigned range is 0 to 255
// SMALLINT - A 2-byte integer, signed range is -32,768 to 32,767, unsigned range is 0 to 65,535
// MEDIUMINT - A 3-byte integer, signed range is -8,388,608 to 8,388,607, unsigned range is 0 to 16,777,215
// INT - A 4-byte integer, signed range is -2,147,483,648 to 2,147,483,647, unsigned range is 0 to 4,294,967,295
// BIGINT - An 8-byte integer, signed range is -9,223,372,036,854,775,808 to 9,223,372,036,854,775,807, unsigned range is 0 to 18,446,744,073,709,551,615
// -------
// DECIMAL - A fixed-point number (M, D) - the maximum number of digits (M) is 65 (default 10), the maximum number of decimals (D) is 30 (default 0)
// FLOAT - A small floating-point number, allowable values are -3.402823466E+38 to -1.175494351E-38, 0, and 1.175494351E-38 to 3.402823466E+38
// DOUBLE - A double-precision floating-point number, allowable values are -1.7976931348623157E+308 to -2.2250738585072014E-308, 0, and 2.2250738585072014E-308 to 1.7976931348623157E+308
// REAL - Synonym for DOUBLE (exception: in REAL_AS_FLOAT SQL mode it is a synonym for FLOAT)
// -------
// BIT - A bit-field type (M), storing M of bits per value (default is 1, maximum is 64)
// BOOLEAN - A synonym for TINYINT(1), a value of zero is considered false, nonzero values are considered true
// SERIAL - An alias for BIGINT UNSIGNED NOT NULL AUTO_INCREMENT UNIQUE
// ------- Date and time --------
// DATE - A date, supported range is 1000-01-01 to 9999-12-31
// DATETIME - A date and time combination, supported range is 1000-01-01 00:00:00 to 9999-12-31 23:59:59
// TIMESTAMP - A timestamp, range is 1970-01-01 00:00:01 UTC to 2038-01-09 03:14:07 UTC, stored as the number of seconds since the epoch (1970-01-01 00:00:00 UTC)
// TIME - A time, range is -838:59:59 to 838:59:59
// YEAR - A year in four-digit (4, default) or two-digit (2) format, the allowable values are 70 (1970) to 69 (2069) or 1901 to 2155 and 0000
// ------- String --------
// CHAR - A fixed-length (0-255, default 1) string that is always right-padded with spaces to the specified length when stored
// VARCHAR - A variable-length (0-65,535) string, the effective maximum length is subject to the maximum row size
// -------
// TINYTEXT - A TEXT column with a maximum length of 255 (2^8 - 1) characters, stored with a one-byte prefix indicating the length of the value in bytes
// TEXT - A TEXT column with a maximum length of 65,535 (2^16 - 1) characters, stored with a two-byte prefix indicating the length of the value in bytes
// MEDIUMTEXT - A TEXT column with a maximum length of 16,777,215 (2^24 - 1) characters, stored with a three-byte prefix indicating the length of the value in bytes
// LONGTEXT - A TEXT column with a maximum length of 4,294,967,295 or 4GiB (2^32 - 1) characters, stored with a four-byte prefix indicating the length of the value in bytes
// -------
// BINARY - Similar to the CHAR type, but stores binary byte strings rather than non-binary character strings
// VARBINARY - Similar to the VARCHAR type, but stores binary byte strings rather than non-binary character strings
// -------
// TINYBLOB - A BLOB column with a maximum length of 255 (2^8 - 1) bytes, stored with a one-byte prefix indicating the length of the value
// MEDIUMBLOB - A BLOB column with a maximum length of 16,777,215 (2^24 - 1) bytes, stored with a three-byte prefix indicating the length of the value
// BLOB - A BLOB column with a maximum length of 65,535 (2^16 - 1) bytes, stored with a two-byte prefix indicating the length of the value
// LONGBLOB - A BLOB column with a maximum length of 4,294,967,295 or 4GiB (2^32 - 1) bytes, stored with a four-byte prefix indicating the length of the value
// -------
// ENUM - An enumeration, chosen from the list of up to 65,535 values or the special '' error value
// SET - A single value chosen from a set of up to 64 members
// ------- Spatial --------
// GEOMETRY - A type that can store a geometry of any type
// POINT - A point in 2-dimensional space
// LINESTRING - A curve with linear interpolation between points
// POLYGON - A polygon
// MULTIPOINT - A collection of points
// MULTILINESTRING - A collection of curves with linear interpolation between points
// MULTIPOLYGON - A collection of polygons
// GEOMETRYCOLLECTION - A collection of geometry objects of any type
// ------- JSON --------
// JSON - Stores and enables efficient access to data in JSON (JavaScript Object Notation) documents</optgroup>
@Service
public class MySQLFieldDefaultValueService {

    public String getDefaultValue(MySQLFieldDTO fieldDTO) {
        String type = fieldDTO.getType().split("\\(")[0].toUpperCase();
        if (fieldDTO.isNullable() || fieldDTO.getDefaultValue() != null) {
            return null;
        }
        switch (type) {
            case "TINYINT":
            case "SMALLINT":
            case "MEDIUMINT":
            case "INT":
            case "BIGINT":
                if (fieldDTO.getExtra().equals("auto_increment")) return null;
                return "0";
            case "DECIMAL":
            case "FLOAT":
            case "DOUBLE":
            case "REAL":
                return "0.0";
            case "DATE":
                return "1000-01-01";
            case "DATETIME":
                return "1000-01-01 00:00:00";
            case "TIMESTAMP":
                return "1970-01-01 00:00:01 UTC";
            case "TIME":
                return "-838:59:59";
            case "YEAR":
                return "1970";
            case "CHAR":
                return "1";
            case "VARCHAR":
            case "TINYTEXT":
            case "TEXT":
            case "MEDIUMTEXT":
            case "LONGTEXT":
                return "";
            default:
                return null;
        }
    }
}
