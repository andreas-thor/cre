drop table if exists cr_size;

create table cr_size (
  cr_clusterid1 int, 
  cr_clusterid2 int , 
  cr_clustersize int, 
  primary key (cr_clusterid1 , cr_clusterid2 )
)
as
select cr_clusterid1, cr_clusterid2, count(*) as cr_clustersize
from cr
group by cr_clusterid1, cr_clusterid2;

update cr 
set cr_clustersize = (
	select cr_size.cr_clustersize 
	from cr_size 
	where cr.cr_clusterid1=cr_size.cr_clusterid1 AND cr.cr_clusterid2 = cr_size.cr_clusterid2
);