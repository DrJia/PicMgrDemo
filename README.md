# 一、小红书效果

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/1.jpg?raw=true)
![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/2.jpg?raw=true)
![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/3.jpg?raw=true)

上面三个图是小红书发布动态的时候选择好图片后，长按图片进行排序的效果。长按后，选择的图片浮起，随手指左右移动，靠近左右边缘的时候，整体的条目可以左右滚动，再将手指选择的图片发到合适的位置。

# 二、改进效果
![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/picMgr.gif?raw=true)

1. 首先是可以上下左右拖动，解决了，只左右滚动，在选择后图片放大导致顶部被切割的问题。

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/4.png?raw=true)

2. 当图片满足9个的时候将最后位置的+号隐藏，小于9个的时候显示+号。小红书并未做此处理。
3. 加入了仿微信拖动到某处删除图片的功能


# 三、实现方案
## 采用recyclerView+ItemTouchHelper
1. 首先创建adapter，定义两种类型的type，一种是pic，一种是+号类型。可设置maxCount，比如最多设置9张图片。

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/5.png?raw=true)

+号始终在最后一位。这样设计，当加入的图片等于maxCount的时候，+号就自动隐藏掉了。

2. 拖动实现

定义ItemTouchHelper.Callback

定义可上下左右四个方向拖动，同时标注+号类型不可拖动。

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/6.png?raw=true)

定义当不同类型、+号类型，图片不足2个的时候，不可发生交换。

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/7.png?raw=true)

设置长按可拖拽

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/8.png?raw=true)

3. 拖动删除逻辑

布局文件定义如下

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/9.png?raw=true)

关键代码为clipChildren = "false" 设置在recyclerview的父布局上，可以使item拖动的时候显示超出recyclerview，这样recyclerview不需要全屏，高度只需要按需设置即可。

同时定义一个拖动到该位置删除Item的view区域，如上图id为delete_area

回到 ItemTouchHelper.Callback 定义listener用于判断，drag开始，结束，和是否item进入删除区域的回调

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/10.png?raw=true)

onSelectedChanged函数用于判断选择的状态

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/11.png?raw=true)

不在闲置状态（即开始拖动状态）下，将被选择的item放大mScale倍，同时通知回调，并记录当前临时的holder。

回到闲置状态的时候（拖动后抬手动作），这个时候回调回来的holder是null，所以需要在拖动开始的时候记录临时变量。用来在拖动到删除区域做删除作用。

clearView是在拖动结束并且item归位动画结束后触发，所以用来将放大的view还原。

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/12.png?raw=true)

当item被拖动的时候会不断触发onChildDraw函数，所以在此函数中判断拖动的item是否和删除区域发生重合，

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/13.png?raw=true)

item和delArea都用此方法获取在窗口中的位置。

由于有item拖动的时候会放大，所以实际计算的时候需要按照放大的item计算

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/14.png?raw=true)

然后通过上下左右的的坐标判断是否发生重叠。这样可以适应任何位置和大小的删除区域，如下图：

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/picMgr1.gif?raw=true)

回到onSelectedChanged方法，判断抬手动作（闲置状态）的时候，判断是否发生重叠，如果重叠则进行删除逻辑。

注意此时删除由于会执行归位动画，item被remove了，但是归位动画又执行了，所以最终item会被归位到原处，后一个item会顶上来发生重合，并且该归位的item的index为-1，此时若点击该归位的item发生崩溃，因为index异常。所以需要将归位动画取消。

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/15.png?raw=true)

如上图，该函数发生在抬手后，即将执行动画之前会获取此函数返回的动画时间，所以设置当发生重叠要删除的时候，动画时间为0就不会执行动画了。

此时问题又来了，动画不执行了，item会立即回到原位并执行消失动画，此时item会先显示出来再消失，所以需要先隐藏掉该item

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/16.png?raw=true)

隐藏掉item，但是holder的view是复用的，会导致后面被复用的holder也隐藏掉。所以removeItemFromDrag做一些处理

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/17.png?raw=true)

为了使删除动画不受影响，不可以使用notifyDataSetChanged，同时为了避免全部刷新导致闪烁，所以使用notifyItemRangeChanged的payload的方式局部刷新。将itemview设置为显示即可。

4. 使用方法

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/18.png?raw=true)

设置，itemHeight，setProportion设置宽高比例，设置水平方向的layoutmanager

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/19.png?raw=true)

设置callback，传入adapter和删除区域view，设置选中放大比例setScale，attach到recyclerview

![image](https://github.com/DrJia/PicMgrDemo/blob/master/readme_src/20.png?raw=true)

# 四、开源代码

组件demo已上传到github，传送门：https://github.com/DrJia/PicMgrDemo

[![LICENSE](https://img.shields.io/badge/license-Anti%20996-blue.svg)](https://github.com/996icu/996.ICU/blob/master/LICENSE)