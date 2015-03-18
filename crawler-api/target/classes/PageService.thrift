namespace java com.yeepay.bigdata.rpc

service PageService {

     bool exists(1: string url)
     bool save(1: string url, 2: string page)

     string getPage(1: string url)
}
