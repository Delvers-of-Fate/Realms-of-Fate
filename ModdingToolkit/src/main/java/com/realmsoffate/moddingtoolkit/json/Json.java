package com.realmsoffate.moddingtoolkit.json;

import java.math.BigDecimal;
import java.util.*;

/** Small dependency-free JSON parser/writer. Values are Map, List, String, BigDecimal, Boolean or null. */
public final class Json {
    private Json() {}
    public static Object parse(String text) { Parser p=new Parser(text); Object v=p.value(); p.ws(); if(!p.end()) throw p.error("Unexpected trailing content"); return v; }
    public static String pretty(Object value) { StringBuilder out=new StringBuilder(); write(value,out,0); return out.toString(); }
    public static Object deepCopy(Object value) { return parse(pretty(value)); }
    private static void write(Object v,StringBuilder out,int depth){
        if(v==null){out.append("null");return;}
        if(v instanceof String){string((String)v,out);return;}
        if(v instanceof Number||v instanceof Boolean){out.append(v);return;}
        if(v instanceof Map){Map<?,?> map=(Map<?,?>)v;out.append('{');if(!map.isEmpty()){out.append('\n');int i=0;for(Map.Entry<?,?> e:map.entrySet()){indent(out,depth+1);string(String.valueOf(e.getKey()),out);out.append(": ");write(e.getValue(),out,depth+1);if(++i<map.size())out.append(',');out.append('\n');}indent(out,depth);}out.append('}');return;}
        if(v instanceof List){List<?> list=(List<?>)v;out.append('[');if(!list.isEmpty()){out.append('\n');for(int i=0;i<list.size();i++){indent(out,depth+1);write(list.get(i),out,depth+1);if(i+1<list.size())out.append(',');out.append('\n');}indent(out,depth);}out.append(']');return;}
        throw new IllegalArgumentException("Unsupported JSON value: "+v.getClass());
    }
    private static void indent(StringBuilder b,int d){for(int i=0;i<d;i++)b.append("  ");}
    private static void string(String s,StringBuilder b){b.append('"');for(char c:s.toCharArray()){switch(c){case '"':b.append("\\\"");break;case '\\':b.append("\\\\");break;case '\b':b.append("\\b");break;case '\f':b.append("\\f");break;case '\n':b.append("\\n");break;case '\r':b.append("\\r");break;case '\t':b.append("\\t");break;default:if(c<32)b.append(String.format("\\u%04x",(int)c));else b.append(c);}}b.append('"');}
    private static final class Parser{
        final String s;int i;Parser(String s){this.s=Objects.requireNonNull(s);}boolean end(){return i>=s.length();}void ws(){while(!end()&&Character.isWhitespace(s.charAt(i)))i++;}RuntimeException error(String m){return new IllegalArgumentException(m+" at character "+i);}
        Object value(){ws();if(end())throw error("Expected JSON value");char c=s.charAt(i);switch(c){case '{':return object();case '[':return array();case '"':return str();case 't':lit("true");return Boolean.TRUE;case 'f':lit("false");return Boolean.FALSE;case 'n':lit("null");return null;default:return number();}}
        Map<String,Object> object(){i++;LinkedHashMap<String,Object> m=new LinkedHashMap<String,Object>();ws();if(peek('}')){i++;return m;}while(true){ws();if(end()||s.charAt(i)!='"')throw error("Expected object property");String k=str();ws();expect(':');Object v=value();m.put(k,v);ws();if(peek('}')){i++;return m;}expect(',');}}
        List<Object> array(){i++;ArrayList<Object>a=new ArrayList<Object>();ws();if(peek(']')){i++;return a;}while(true){a.add(value());ws();if(peek(']')){i++;return a;}expect(',');}}
        String str(){expect('"');StringBuilder b=new StringBuilder();while(!end()){char c=s.charAt(i++);if(c=='"')return b.toString();if(c!='\\'){b.append(c);continue;}if(end())throw error("Bad escape");char e=s.charAt(i++);switch(e){case '"':case '\\':case '/':b.append(e);break;case 'b':b.append('\b');break;case 'f':b.append('\f');break;case 'n':b.append('\n');break;case 'r':b.append('\r');break;case 't':b.append('\t');break;case 'u':if(i+4>s.length())throw error("Bad unicode escape");b.append((char)Integer.parseInt(s.substring(i,i+4),16));i+=4;break;default:throw error("Bad escape");}}throw error("Unterminated string");}
        BigDecimal number(){int st=i;if(peek('-'))i++;digits();if(peek('.')){i++;digits();}if(peek('e')||peek('E')){i++;if(peek('+')||peek('-'))i++;digits();}try{return new BigDecimal(s.substring(st,i));}catch(Exception e){throw error("Invalid number");}}
        void digits(){int st=i;while(!end()&&Character.isDigit(s.charAt(i)))i++;if(st==i)throw error("Expected digit");}void lit(String x){if(!s.startsWith(x,i))throw error("Expected "+x);i+=x.length();}boolean peek(char c){return !end()&&s.charAt(i)==c;}void expect(char c){ws();if(end()||s.charAt(i)!=c)throw error("Expected '"+c+"'");i++;}
    }
}
