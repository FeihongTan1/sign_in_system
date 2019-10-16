<template>
    <el-dialog title="选择地点"
               top="10px"
               :append-to-body="true"
               :close-on-click-modal="false"
               :close-on-press-escape="false"
               :visible="dialogVisible"
               :before-close="closeDialog">
        <el-header height="auto">
            <el-form :inline="true" size="mini" @submit.native.prevent label-width="100px" label-position="right">
                <el-form-item label="关键词">
                    <el-input @keypress.enter.native="onSearch" v-model="keyword"></el-input>
                </el-form-item>
                <el-form-item>
                    <el-button @keypress.enter.native="onSearch" type="info" @click="onSearch">
                        搜索
                    </el-button>
                </el-form-item>
            </el-form>
            <el-form :inline="true" size="mini" @submit.native.prevent label-width="100px" label-position="right">
                <el-form-item label="选定经纬度">
                    <span v-text="`经${this.long},纬${this.lat}`"></span>
                </el-form-item>
                <el-form-item>
                    <el-button @keypress.enter.native="onSure" type="primary" @click="onSure">
                        确定
                    </el-button>
                </el-form-item>
            </el-form>
        </el-header>
        <el-main class="map-dialog-main">
            <div id="qq_map" :style="`width:100%; height:${mapHeight}px`"></div>
            <div id="qq_result" style="width: 100%; height: 300px">
                <el-table :data="pois" size="mini" width="100%" height="300" @row-click="handleRowClick">
                    <el-table-column prop="name" label="名字"></el-table-column>
                    <el-table-column prop="address" label="地址"></el-table-column>
                    <el-table-column prop="latLng.lng" label="经度"></el-table-column>
                    <el-table-column prop="latLng.lat" label="纬度"></el-table-column>
                </el-table>
            </div>
        </el-main>
        <el-footer>
            <el-pagination
                    class="coz-manage-pagination"
                    @current-change="getPage"
                    :current-page.sync="pagination.currentPage"
                    :page-size="pagination.size"
                    :page-sizes="[10, 30, 50, 100, 200]"
                    layout="total, prev, pager, next, jumper"
                    :total="pagination.total">
            </el-pagination>
        </el-footer>
    </el-dialog>
</template>

<script>
    function initMap() {
        return new Promise((resolve, reject) => {
            window.init = () => {
                // eslint-disable-next-line no-undef
                resolve(qq)
            }
            let script = document.createElement("script")
            script.type = "text/javascript"
            script.src = 'https://map.qq.com/api/js?v=2.exp&callback=init&key=Q62BZ-BPCKJ-2IQFY-FQQFL-2UAWH-RJBJD'
            script.onerror = reject
            document.head.appendChild(script)
        })
    }

    export default {
        name: "MapDialog",
        props: {
            dialogVisible: Boolean
        },
        watch: {
            dialogVisible() {
                if (this.dialogVisible) {
                    const _this = this
                    initMap()
                        .then(qq => {
                                _this.map = new qq.maps.Map(document.getElementById('qq_map'), {
                                    center: new qq.maps.LatLng(30.6733485125, 104.1015100479),
                                    zoom: 15
                                });
                                const latlngBounds = new qq.maps.LatLngBounds();
                                const infoWin = _this.infoWin = new qq.maps.InfoWindow({
                                    map: _this.map
                                });
                                _this.searchService = new qq.maps.SearchService({
                                    //设置搜索范围为北京
                                    location: "成都",
                                    //设置搜索页码为1
                                    pageIndex: 0,
                                    //设置每页的结果数为5
                                    pageCapacity: 5,
                                    //设置动扩大检索区域。默认值true，会自动检索指定城市以外区域。
                                    autoExtend: false,
                                    //检索成功的回调函数
                                    complete(results) {
                                        _this.pagination.total = results.detail.totalNum
                                        _this.pagination.currentPage = results.detail.pageIndex + 1
                                        //设置回调函数参数
                                        const pois = _this.pois = results.detail.pois;
                                        for (let i = 0, l = pois.length; i < l; i++) {
                                            const poi = pois[i];
                                            //扩展边界范围，用来包含搜索到的Poi点
                                            latlngBounds.extend(poi.latLng);
                                            const marker = new qq.maps.Marker({
                                                map: _this.map,
                                                position: poi.latLng
                                            });

                                            marker.setTitle(i + 1);
                                            qq.maps.event.addListener(marker, 'click', () => {
                                                infoWin.open();
                                                infoWin.setContent(`<div style="width:280px;height:80px;">POI的ID为：${pois[i].id}，POI的名称为：${pois[i].name}，POI的地址为：${pois[i].address}，POI的类型为：${pois[i].type}</div>`);
                                                infoWin.setPosition(pois[i].latLng);
                                                _this.long = pois[i].latLng.getLng()
                                                _this.lat = pois[i].latLng.getLat()
                                                _this.map.panTo(pois[i].latLng)
                                                if (_this.map.getZoom() < 14)
                                                    _this.map.zoomTo(14)
                                                else
                                                    _this.map.zoomBy(2)
                                            });

                                            _this.markers.push(marker);

                                        }
                                        //调整地图视野
                                        _this.map.fitBounds(latlngBounds);
                                    },
                                    //若服务请求失败，则运行以下函数
                                    error(err) {
                                        this.$message.error(err)
                                    }
                                })

                                qq.maps.event.addListener(_this.map, 'click', (event) => {
                                        _this.long = event.latLng.getLng()
                                        _this.lat = event.latLng.getLat()
                                    }
                                );
                            }
                        )
                }
            }
        },
        data() {
            return {
                keyword: '',
                mapHeight: document.documentElement.clientHeight * 0.7 - 400 < 300 ? 300 : document.documentElement.clientHeight * 0.9 - 400,
                map: null,
                searchService: null,
                markers: [],
                pois: [],
                lat: 0,
                long: 0,
                infoWin: null,
                pagination: {
                    currentPage: 1,
                    total: 0,
                    size: 5
                }
            }
        }
        ,
        methods: {
            getPage() {
                this.searchService.setPageIndex(this.pagination.currentPage - 1);
                this.searchService.search(this.keyword);
            },
            closeDialog() {
                this.keyword = ''
                this.map = null
                this.searchService = null
                this.markers = []
                this.pois = []
                this.lat = 0
                this.long = 0
                this.infoWin = null
                this.pagination = {
                    currentPage: 1,
                    total: 0,
                    size: 5
                }
                this.$emit('closeDialog')
            },
            onSearch() {
                if ('' === this.keyword.trim())
                    return
                this.markers = []
                //根据输入的城市设置搜索范围
                // searchService.setLocation("北京");
                //根据输入的关键字在搜索范围内检索
                this.searchService.search(this.keyword);
            },
            onSure() {
                this.$emit('closeDialog', {
                    lat: this.lat,
                    long: this.long
                })
                this.keyword = ''
                this.map = null
                this.searchService = null
                this.markers = []
                this.pois = []
                this.lat = 0
                this.long = 0
                this.infoWin = null
                this.pagination = {
                    currentPage: 1,
                    total: 0,
                    size: 5
                }
            },
            handleRowClick(row) {
                this.infoWin.open();
                this.infoWin.setContent(`<div style="width:280px;height:80px;">POI的ID为：${row.id}，POI的名称为：${row.name}，POI的地址为：${row.address}，POI的类型为：${row.type}</div>`);
                this.infoWin.setPosition(row.latLng);
                this.long = row.latLng.getLng()
                this.lat = row.latLng.getLat()
                this.map.panTo(row.latLng)
                if (this.map.getZoom() < 14)
                    this.map.zoomTo(14)
                else
                    this.map.zoomBy(2)
            }
        }
    }
</script>

<style scoped>
    .map-dialog-main {
        padding-top: 0;
    }
</style>