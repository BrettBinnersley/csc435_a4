
target datalayout = "e-p:64:64:64-i1:8:8-i8:8:8-i16:16:16-i32:32:32-i64:64:64-f32:32:32-f64:64:64-v64:64:64-v128:128:128-a0:0:64-s0:64:64-f80:128:128-n8:16:32:64-S128"

target triple = "x86_64-unknown-linux-gnu"

@arr.1 = common global [ 10 x float ] zeroinitializer, align 8
@.str.2 = private unnamed_addr constant [9 x i8] c"all done\00", align 1
@.str.3 = private unnamed_addr constant [5 x i8] c"%s \0a\00", align 1
@.str.4 = private unnamed_addr constant [12 x i8] c"k, arr[k] =\00", align 1
@.str.5 = private unnamed_addr constant [11 x i8] c"%s %d %g \0a\00", align 1
declare i64 @printf(i8*, ...) #1
declare void @llvm.memset.p0i8.i64(i8*, i8, i64, i32, i1)

; Function Attrs: nounwind uwtable
define void @main() {
entry:
  call void @fill(i32 10)
  call void @dump(i32 10)
  %0 = call i64 (i8*, ...) @printf(i8* getelementptr inbounds ([5 x i8], [5 x i8]* @.str.3, i32 0, i32 0) , [9 x i8]* @.str.2)
  ret void
}
; Function Attrs: nounwind uwtable
define void @fill(i32 %k) {
entry:
  %k.addr = alloca i32, align 4
  store i32 %k, i32* %k.addr, align 4
  %0 = load i32, i32* %k.addr
  %1 = icmp sge i32 %0, 0
  br i1 %1, label %then.0, label %else.1
then.0:
  %2 = load i32, i32* %k.addr
  %3 = sub i32 %2, 1
  store i32 %3, i32* %k.addr, align 4
  %4 = load i32, i32* %k.addr
  %5 = getelementptr inbounds [ 10 x float ], [ 10 x float ]* @arr.1, i32 0, i32 %4
  %6 = load i32, i32* %k.addr
  %7 = load i32, i32* %k.addr
  %8 = mul i32 %6, %7
  %9 = sitofp i32 %8 to float
  store float %9, float* %5, align 4
  %10 = load i32, i32* %k.addr
  call void @fill(i32 %10)
  br label %endif.2
else.1:
  br label %endif.2
endif.2:
  ret void
}
; Function Attrs: nounwind uwtable
define void @dump(i32 %k) {
entry:
  %k.addr = alloca i32, align 4
  store i32 %k, i32* %k.addr, align 4
  %0 = load i32, i32* %k.addr
  %1 = icmp sgt i32 %0, 0
  br i1 %1, label %then.0, label %else.1
then.0:
  %2 = load i32, i32* %k.addr
  %3 = sub i32 %2, 1
  store i32 %3, i32* %k.addr, align 4
  %4 = load i32, i32* %k.addr
  call void @dump(i32 %4)
  %5 = load i32, i32* %k.addr
  %6 = getelementptr inbounds [ 10 x float ], [ 10 x float ]* @arr.1, i32 0, i32 %5
  %7 = load i32, i32* %k.addr
  %8 = load float, float* %6
  %9 = fpext float %8 to double
  %10 = call i64 (i8*, ...) @printf(i8* getelementptr inbounds ([11 x i8], [11 x i8]* @.str.5, i32 0, i32 0) , [12 x i8]* @.str.4, i32 %7, double %9)
  br label %endif.2
else.1:
  br label %endif.2
endif.2:
  ret void
}


attributes #0 = { nounwind uwtable "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "unsafe-fp-math"="false" "use-soft-float"="false" }
attributes #1 = { "less-precise-fpmad"="false" "no-frame-pointer-elim"="true" "no-frame-pointer-elim-non-leaf" "no-infs-fp-math"="false" "no-nans-fp-math"="false" "stack-protector-buffer-size"="8" "unsafe-fp-math"="false" "use-soft-float"="false" }

