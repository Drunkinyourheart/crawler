#!/bin/bash

if [ $# -eq 2 ] 
then
	newsDay=$1
	thisDay=$2
else
	newsDay=`date -d "-1 day" +%Y-%m-%d`
	thisDay=`date +%Y-%m-%d`
fi

###ensure real path
realpath="/data/apps/jetty/webapps/newscontent/newsStat/"
statpath="/opt/apps/jetty/webapps/newscontent/newsStat/"
[ ! -d "$realpath" ] && (mkdir -p "$realpath";ln -s $realpath $statpath)


logpath='/opt/apps/spider/schedule/logs'
logfile="$logpath/spider-schedule-stat.log.$newsDay"
#logfile="$logpath/spider-schedule-stat.log"

datapath="$statpath$newsDay"
[ ! -d "$datapath" ] && mkdir -p "$datapath"
#datapath='.'
statResult="$datapath/news_stat_info_$newsDay.txt"
crawleError="$datapath/crawle_error_$newsDay.txt"
delayError="$datapath/delay_error_$newsDay.txt"
crawleTimeConsuming="$datapath/crawle_time_consuming_$newsDay.txt"

####crawle result http status
echo "crawle https status stat:" > $statResult
grep "crawlerResult" $logfile | awk -F ' -\\|- ' -v efile="$crawleError" -f httpStateCode >> $statResult

####crawle detail result http status
echo "crawle detail https status stat:" >> $statResult
grep "crawlerResult" $logfile | grep "detail" | awk -F ' -\\|- ' -v efile="$crawleError" -f httpStateCode >> $statResult

###stat total news count
echo -e "\ntotal news count : " >> $statResult
mysql -h 10.13.82.17 -u recom -pxsKR6QSufx --default-character-set=utf8 -s -e "select count(*) from spider.news_index where createTime>='$newsDay' and createTime<'$thisDay';" >> $statResult

###delay time distribution
echo -e "\ndelay time distribution : " >> $statResult
echo "0 distribution : 表示没有抽取到新闻时间，错误url在：" >> $statResult
echo "36000 distribution : 表示延迟时间过长，可能是抽取时间戳错误" >> $statResult
grep "extract" $logfile | grep "Success" | grep DETAIL | grep "domain : " | awk -F ' -\\|- ' -v efile="$delayError" -f delayDistribute | sort -t : -k 1,1 -n >>$statResult

###domain distribution
echo -e "\ndomain distribution : " >> $statResult
grep "extract" $logfile | grep "Success" | grep DETAIL | grep "domain : " | awk -F ' -\\|- ' -f domainDistribute | sort -t : -k 2,2 -nr >>$statResult

###fromURL distribution
echo -e "\nfromURl crawler time consuming distribution : " >> $crawleTimeConsuming
echo -e "\n第一列是抓取的URL : " >> $crawleTimeConsuming
echo -e "\n第二列是抓取URL到解析完准备入库之间的耗费时间 : " >> $crawleTimeConsuming
echo -e "\n第三列是抓取URL的结果从准备入库到入库完毕之间的耗费时间 : " >> $crawleTimeConsuming
grep theWholeTimeForCrawlerStartToExtractResultEnd $logfile |grep fromURL  | awk -F ' -\\|- ' -f fromUrlTimeConsumingDistribute | sort -t : -k 3 -nr >>$crawleTimeConsuming


