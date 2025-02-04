package com.theincgi.advancedmacros.lua.functions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import org.apache.commons.io.IOUtils;

import org.luaj.vm2_v3_0_1.LuaError;
import org.luaj.vm2_v3_0_1.LuaTable;
import org.luaj.vm2_v3_0_1.LuaValue;
import org.luaj.vm2_v3_0_1.Varargs;
import org.luaj.vm2_v3_0_1.lib.OneArgFunction;
import org.luaj.vm2_v3_0_1.lib.VarArgFunction;
import org.luaj.vm2_v3_0_1.lib.ZeroArgFunction;

public class HTTP extends OneArgFunction{
    //
    @Override
    public LuaValue call(LuaValue arg) {
        try {
            LuaTable sets = arg.checktable();

            URL url = new URL(sets.get("url").checkjstring());
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();

            conn.setRequestMethod(sets.get("requestMethod").optjstring("GET"));
            if(sets.get("requestProperties").istable()) {
                LuaTable t = sets.get("requestProperties").checktable();
                for(LuaValue v : t.keys()) {
                    conn.setRequestProperty(v.checkjstring(), t.get(v).checkjstring());
                }
            }
            conn.setConnectTimeout(sets.get("timeout").optint(10) * 1000); //in seconds, default 10s

            conn.setDoOutput(sets.get("doOutput").optboolean(false));
            conn.setInstanceFollowRedirects(sets.get("followRedirects").optboolean(conn.getFollowRedirects()));



            return new LuaConnection(conn);

        } catch (MalformedURLException e) {
            throw new LuaError("MalformedURLException occurred");
        } catch (IOException e) {
            e.printStackTrace();
            throw new LuaError("IOException occurred: " + e.getCause());
        }
    }

    public static class LuaConnection extends LuaTable{
        HttpURLConnection conn;

        public LuaConnection(HttpURLConnection conn) throws IOException {
            this.conn = conn;
            this.set("input", new ZeroArgFunction() {@Override public LuaValue call() {try {
                return new LuaInputStream(conn.getInputStream());
            } catch (IOException e) {
                e.printStackTrace();
                throw new LuaError("IOException while retrieving Input Stream: " + e.getCause());
            }}});
            if(conn.getDoOutput())
                this.set("output", new ZeroArgFunction() {@Override public LuaValue call() {try {
                    return new LuaOutputStream(conn.getOutputStream());
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new LuaError("IOException while retrieving Output Stream: " + e.getCause());
                }}});
            this.set("err", new ZeroArgFunction() {@Override public LuaValue call() {return  conn.getErrorStream() != null ? new LuaInputStream(conn.getErrorStream()) : LuaValue.NIL;}} );
            this.set("getURL", new ZeroArgFunction() {@Override	public LuaValue call() {return LuaValue.valueOf(conn.getURL().toString());}} );
            this.set("getResponseCode", new ZeroArgFunction() {@Override	public LuaValue call() {try {return LuaValue.valueOf(conn.getResponseCode());}catch (IOException e) {return LuaValue.NIL;}}} );
            this.set("getContentType", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    try {
                        return LuaValue.valueOf(conn.getContentType());
                    } catch(Exception e) {
                        return LuaValue.valueOf("none");
                    }
                }
            });
            this.set("getContentEncoding", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(conn.getContentEncoding());
                }
            });
            this.set("getContentLength", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(conn.getContentLengthLong());
                }
            });
            this.set("getFollowRedirects", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    return LuaValue.valueOf(conn.getFollowRedirects());
                }
            });
            this.set("disconnect", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    conn.disconnect();
                    return LuaValue.NONE;
                }
            });
            this.set("getHeaderFields", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    //System.out.println(conn.getHeaderFields().toString());
                    LuaTable t = new LuaTable();
                    try{
                        conn.getHeaderFields().forEach((String field, List<String> value) ->
                        {
                            String s = value.get(0);
                            if (field==null)
                                t.set("http",s);
                            else if (value.size() == 1)
                                t.set(field,s);
                            else{
                                LuaTable u = new LuaTable();
                                for(int i=0, j=1; j<value.size(); j++)
                                    u.set(i++,value.get(j));
                                t.set(field,u);
                            }
                        });
                        return t;
                    }catch(Exception e) { e.printStackTrace(); }
                    return LuaValue.FALSE;
                }
            });
        }

        @Override
        protected void finalize() throws Throwable {
            try{
                conn.disconnect();
            }catch(Exception e) {}
        }

    }

    public static class LuaInputStream extends LuaTable{
        InputStream in;
        Scanner scanner;
        public LuaInputStream(InputStream in) {
            this.in = in;
            scanner = new Scanner(in);

            this.set("readByte", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    try {
                        return LuaValue.valueOf(in.read());
                    } catch (IOException e) {
                        return LuaValue.valueOf(-1);
                    }
                }
            });
            this.set("readChar", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    try {
                        int b = in.read();
                        if(b>=0)
                            return LuaValue.valueOf(String.valueOf((char)b));
                        return FALSE;
                    } catch (IOException e) {
                        return LuaValue.NIL;
                    }
                }
            });
            this.set("available", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    try {
                        return LuaValue.valueOf(in.available());
                    } catch (IOException e) {
                        return LuaValue.valueOf(0);
                    }
                }
            });
            this.set("readLine", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    if(scanner.hasNextLine())
                        return LuaValue.valueOf(scanner.nextLine());
                    return LuaValue.NIL;
                }
            });
            this.set("close", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    scanner.close();
                    try {
                        in.close();
                    } catch (IOException e) {}
                    return LuaValue.NONE;
                }
            });
            this.set("getContent", new ZeroArgFunction() {
                @Override
                public LuaValue call() {
                    try {
                        // return LuaValue.valueOf(IOUtils.toString(in,Charset.forName(charset.optjstring("UTF-8"))));
                        return LuaValue.valueOf(IOUtils.toByteArray(in));
                    } catch (IOException e) {}
                    return LuaValue.NONE;
                }
            });
        }

    }

    public static class LuaOutputStream extends LuaTable{
        OutputStream out;

        public LuaOutputStream(OutputStream out) {
            this.out = out;
            this.set("writeByte", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    try {
                        out.write(arg.checkint());
                    } catch (IOException e) {
                        throw new LuaError("IOException occurred");
                    }
                    return LuaValue.NONE;
                }
            });
            this.set("writeBytes", new VarArgFunction() {
                @Override
                public Varargs invoke(Varargs args) {
                    if(args.arg1().istable()) {
                        args = args.arg1().checktable().unpack();
                    }
                    for (int i = 0; i < args.narg(); i++) {
                        try {
                            out.write(args.arg(i).checkint());
                        } catch (IOException e) {
                            throw new LuaError("IOException occurred");
                        }
                    }
                    return LuaValue.NONE;
                }
            });
            this.set("write", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    try {
                        out.write(arg.checkstring().m_bytes);
                    } catch (IOException e) {
                        throw new LuaError("IOException occurred");
                    }
                    return LuaValue.NONE;
                }
            });
            this.set("writeLine", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    try {
                        out.write(arg.checkstring().m_bytes);
                        out.write('\n');
                    } catch (IOException e) {
                        throw new LuaError("IOException occurred");
                    }
                    return LuaValue.NONE;
                }
            });
            this.set("flush", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    try {
                        out.flush();
                    } catch (IOException e) {
                        throw new LuaError("IOException occurred");
                    }
                    return LuaValue.NONE;
                }
            });
            this.set("close", new OneArgFunction() {
                @Override
                public LuaValue call(LuaValue arg) {
                    try {
                        out.close();
                    } catch (IOException e) {
                        throw new LuaError("IOException occurred");
                    }
                    return LuaValue.NONE;
                }
            });

        }

    }
}