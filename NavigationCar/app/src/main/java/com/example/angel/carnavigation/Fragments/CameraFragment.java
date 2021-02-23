package com.example.angel.carnavigation.Fragments;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.angel.carnavigation.GlobalVars.GlobalVars;
import com.example.angel.carnavigation.R;
import com.example.angel.carnavigation.CameraUtils.Yuv2RgbConverter;
import com.example.angel.carnavigation.CameraUtils.Yuv2RgbConverter2;
import com.example.angel.carnavigation.CameraUtils.Line;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import com.google.firebase.ml.vision.common.FirebaseVisionImageMetadata;
import com.google.firebase.ml.vision.text.FirebaseVisionText;
import com.google.firebase.ml.vision.text.FirebaseVisionTextRecognizer;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.camera.core.AspectRatio;
import androidx.camera.core.Camera;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import butterknife.BindView;
import butterknife.ButterKnife;

public class CameraFragment extends Fragment {

    @BindView(R.id.button1)
    Button speed_signal;

    @BindView(R.id.button2)
    Button speed_real;

	Executor executor1 = Executors.newSingleThreadExecutor();
	Executor executor2 = Executors.newSingleThreadExecutor();

	private GlobalVars gVars;

	Preview preview;
	ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
	ProcessCameraProvider cameraProvider;
	CameraSelector cameraSelector;
	Bitmap bitmap;
	Bitmap textBitmap;

	private String[] values = new String[] {"30", "40",
			"50", "60", "70", "80", "90",
			"100", "110", "120"};

	private Line leftLane;
	private Line rightLane;


	@BindView(R.id.ivBitmap)
	ImageView ivBitmap;

	@BindView(R.id.preview_view)
	PreviewView previewView;

	private static String TAG = CameraFragment.class.getSimpleName().toString().trim();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
							 Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_camera, container, false);
        ButterKnife.bind(this, rootView);

		gVars = new GlobalVars().getInstance();

		leftLane = new Line(0, 0, 0, 0);
		rightLane = new Line(0, 0, 0, 0);

		bindCameraUseCases();

		return rootView;
	}

	@Override
	public void onResume() {
		super.onResume();
		Log.d(TAG, "onResume");
		//startCameraSource();
	}

	/** Stops the camera. */
	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	private void bindCameraUseCases(){
		previewView.post((Runnable)new Runnable(){

			@Override
			public void run() {
				cameraProviderFuture = ProcessCameraProvider.getInstance(getContext());
				cameraProviderFuture.addListener(new Runnable() {
					@Override
					public void run() {
						try {
							cameraProvider = cameraProviderFuture.get();
						} catch (ExecutionException e) {
							e.printStackTrace();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						bindPreview(cameraProvider);
					}
				},  ContextCompat.getMainExecutor(getContext()));
			}
		});
	}

	@SuppressLint("UnsafeExperimentalUsageError")
	void bindPreview (ProcessCameraProvider cameraProvider) {
		preview = new Preview.Builder()
				.setTargetAspectRatio(AspectRatio.RATIO_16_9)
				.setTargetRotation(Surface.ROTATION_180)
				.build();

		cameraSelector = new CameraSelector.Builder()
				.requireLensFacing(CameraSelector.LENS_FACING_BACK)
				.build();


		ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
				.setTargetAspectRatio(AspectRatio.RATIO_16_9)
				.setTargetRotation(Surface.ROTATION_180)
				.setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
				.build();

		if(gVars.getRoadLine()){
			imageAnalysis.setAnalyzer(executor1, roadImageAnalysis());
		}

		//imageAnalysis.setAnalyzer(executor1, roadImageAnalysis());

		ImageAnalysis textAnalysis = new ImageAnalysis.Builder()
				.build();

		if(gVars.getAlertSpeed()){
			textAnalysis.setAnalyzer(executor2, textAnalysis());
		}

		//textAnalysis.setAnalyzer(executor2, textAnalysis());

		cameraProvider.unbindAll();
		Camera camera = cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalysis, textAnalysis);

		preview.setSurfaceProvider(previewView.createSurfaceProvider(camera.getCameraInfo()));
	}

	private ImageAnalysis.Analyzer roadImageAnalysis(){
		ImageAnalysis.Analyzer imageAnalysis = new ImageAnalysis.Analyzer() {
			@Override
			public void analyze(ImageProxy image) {
				//image.getHeight();

				//final Bitmap bitmap = null;
				if (bitmap == null) {
					bitmap = Bitmap.createBitmap(
							image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
				}
				//final Bitmap bitmap = toBitmap(image);
				//if(bitmap==null)
				//return;

				bitmap = Yuv2RgbConverter2.yuvToRgb(getContext(), image.getImage(), bitmap);

				if (bitmap != null) {

					Mat mat = new Mat();
					Mat resultMat = new Mat();
					Matrix matrix = new Matrix();

					Utils.bitmapToMat(bitmap, mat);

					//resultMat = roadLaneAnalysis(mat, image.getWidth(), image.getHeight());
					//Imgproc.cvtColor(mat, resultMat, Imgproc.COLOR_RGB2GRAY);

					//Utils.matToBitmap(resultMat, bitmap);
					if (previewView.getDisplay() != null) {
						float width = image.getWidth();
						float height = image.getHeight();
						MatOfPoint counterPoints;
						Point floodPoint;

						switch (previewView.getDisplay().getRotation()) {
							case Surface.ROTATION_0:
								//counterPoints = new MatOfPoint(new Point(width, 0), new Point(width/2, 0), new Point(width/2, height), new Point(width, height));
								counterPoints = new MatOfPoint(new Point(width, -400), new Point(width / 2, height / 2), new Point(width, height+400));

								floodPoint = new Point(width * 0.75, height / 2);
								resultMat = startAnalysis(mat, width, height, counterPoints, floodPoint, false);
								matrix.preRotate(90);
								break;
							case Surface.ROTATION_90:
								counterPoints = new MatOfPoint(new Point(0, height), new Point(width / 2, 0), new Point(width, height));
								floodPoint = new Point(width / 2, height * 0.75);
								resultMat = startAnalysis(mat, width, height, counterPoints, floodPoint, true);
								break;
							case Surface.ROTATION_270:
								counterPoints = new MatOfPoint(new Point(0, 0), new Point(width / 2, height), new Point(width, 0));
								floodPoint = new Point(width / 2, height * 0.25);
								resultMat = startAnalysis(mat, width, height, counterPoints, floodPoint, true);
								matrix.preRotate(180);
								break;
						}


						Utils.matToBitmap(resultMat, bitmap);

						Bitmap uprightImage = Bitmap.createBitmap(
								bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);

						Bitmap finalBitmap = uprightImage;
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								ivBitmap.setImageBitmap(finalBitmap);

							}
						});

						image.close();
					}
				}
			}

		};

		return imageAnalysis;
	}

	private Mat startAnalysis(Mat mat, float width, float height, MatOfPoint cPoint, Point fPoint, Boolean land){
		Mat maskCopyTo2 = Mat.zeros(mat.size(), CvType.CV_8UC1);
		List<MatOfPoint> counter = new ArrayList<>();
		counter.add(cPoint);
		Imgproc.drawContours(maskCopyTo2, counter, -1, Scalar.all(255));

		Mat maskFloodFill2 = Mat.zeros(mat.rows() + 2, mat.cols() + 2, CvType.CV_8UC1);
		Imgproc.floodFill(maskCopyTo2, maskFloodFill2, fPoint, Scalar.all(255), null, Scalar.all(20), Scalar.all(20), 4);

		Mat resultMat = roadLaneAnalysis(mat, maskCopyTo2, counter, width, height, land);
		return resultMat;
	}


	private Mat roadLaneAnalysis(Mat mat, Mat maskCopyTo2, List<MatOfPoint> counter,float width, float height, boolean land){

		Mat linesP = new Mat();

		Mat imgIrregularROI = new Mat();
		mat.copyTo(imgIrregularROI, maskCopyTo2);

		Mat gray = new Mat();
		Mat cannyEdges = new Mat();
		Mat blur = new Mat();
		org.opencv.core.Size size = new Size(15,15);

		Imgproc.cvtColor(imgIrregularROI, gray, Imgproc.COLOR_RGB2GRAY);
		Imgproc.Canny(gray, cannyEdges, 100, 150, 3, false);


		Imgproc.HoughLinesP(cannyEdges, linesP, 6, Math.PI/60, 160, 40, 25);

		double boundary = 1/2.1;
		double left_region_boundary = height * (1 - boundary) ;
		double right_region_boundary = height * boundary;
		boolean cont;
		double dist_max = 0.d;
		//Point start = new Point(0,0);
		//Point end = new Point(0,0);

		if (linesP.cols() > 0 && linesP.rows() > 20) {
			for (int x = 0; x < 20; x++) {
				double[] vec = linesP.get(x, 0);
				double x1 = vec[0],
						y1 = vec[1],
						x2 = vec[2],
						y2 = vec[3];

				cont = true;

				Point start = new Point(x1, y1);
				Point end = new Point(x2, y2);

				Point left1 = new Point(0, left_region_boundary);
				Point left2 = new Point(width, left_region_boundary);

				Point right1 = new Point(0, right_region_boundary);
				Point right2 = new Point(width, right_region_boundary);

				Imgproc.line(mat, left1, left2, new Scalar(255, 255, 255), 10);
				Imgproc.line(mat, right1, right2, new Scalar(255, 255, 0), 10);
				Imgproc.line(mat, new Point(width, -400), new Point(width / 2, height/2), new Scalar(255, 255, 0), 10);
				Imgproc.line(mat, new Point(width, height+400), new Point(width/2, height/2), new Scalar(255, 255, 0), 10);


/*				double dx = x1 - x2;
				double dy = y1 - y2;

				double dist = Math.sqrt(dx * dx + dy * dy);

				if (dist > dist_max) {
					dist_max = dist;
					start = pt1;
					end = pt2;
				}*/

				for (MatOfPoint n : counter) {
					if (Imgproc.pointPolygonTest(new MatOfPoint2f(n.toArray()), start, true) < 20 || Imgproc.pointPolygonTest(new MatOfPoint2f(n.toArray()), end, true) < 20) {
						cont = false;
					}
				}

				if (cont) {

					if ((Math.abs(end.x - start.x) >= Math.abs(end.y - start.y))) {

						//Imgproc.line(cannyEdges, new Point(x1, y1), new Point(x2, y2), new Scalar(255, 0, 0, 100), 15);

					}
					if (!land) {
						if ((start.y < right_region_boundary && end.y < right_region_boundary) && (Math.abs(end.y - start.y) < Math.abs(end.x - start.x))) {
							//leftLane.setLine(0,width,y1,y2);
							rightLane.setLine(start.x, end.x, start.y, end.y);
						}

						if (start.y > left_region_boundary && end.y > left_region_boundary && (Math.abs(end.y - start.y) < Math.abs(end.x - start.x))) {
							//rightLane.setLine(0,width,y1,y2);
							leftLane.setLine(start.x, end.x, start.y, end.y);
						}
					} else if (land) {
						if (start.x < left_region_boundary && end.x < left_region_boundary) {
							leftLane.setLine(start.x, end.x, start.y, end.y);
						}

						if (start.x > right_region_boundary && end.x > right_region_boundary) {
							rightLane.setLine(start.x, end.x, start.y, end.y);
						}

					}

				}
				Point i = Line.getIntersectionPoint(leftLane, rightLane);

				if (i != null && i.x > 0 && i.x < width && i.y > 0 && i.y < height) {
					//Draw the lines with horizon
					//Imgproc.line(cannyEdges, leftLane.getStart(), i, new Scalar(255, 0, 0), 20);
					//Imgproc.line(cannyEdges, rightLane.getStart(), i, new Scalar(255, 0, 0), 20);
					//Imgproc.rectangle(cannyEdges,leftLane.getStart(), rightLane.getStart(),new Scalar(255, 0, 0));
				} else {
					//Draw the lines without horizon
					Imgproc.line(mat, leftLane.getStart(), leftLane.getEnd(), new Scalar(255, 0, 0, 100), 20);
					Imgproc.line(mat, rightLane.getStart(), rightLane.getEnd(), new Scalar(255, 0, 0, 100), 20);
					Point[] rook_points = new Point[4];
					rook_points[0] = rightLane.getStart();
					rook_points[3] = rightLane.getEnd();
					rook_points[1] = leftLane.getStart();
					rook_points[2] = leftLane.getEnd();
					MatOfPoint matPt = new MatOfPoint();
					matPt.fromArray(rook_points);
					List<MatOfPoint> ppt = new ArrayList<MatOfPoint>();
					ppt.add(matPt);
					//Imgproc.rectangle(mat,leftLane.getEnd(), rightLane.getStart(),new Scalar(0, 255, 0), -1, 8 ,0);
					Imgproc.fillPoly(mat, ppt, new Scalar(0, 255, 0, 100));

				}
			}
		}
		return mat;
	}

	/*private Mat roadLaneAnalysis(Mat mat, Mat maskCopyTo2, List<MatOfPoint> counter,float width, float height, boolean land) {

		Mat linesP = new Mat();

		Mat imgIrregularROI = new Mat();
		mat.copyTo(imgIrregularROI, maskCopyTo2);

		Mat gray = new Mat();
		Mat cannyEdges = new Mat();
		Mat blur = new Mat();
		org.opencv.core.Size size = new Size(15, 15);

		Imgproc.cvtColor(imgIrregularROI, gray, Imgproc.COLOR_RGB2GRAY);
		Imgproc.Canny(gray, cannyEdges, 100, 150, 3, false);


		Imgproc.HoughLinesP(cannyEdges, linesP, 6, Math.PI / 60, 160, 40, 25);

		double boundary = 1 / 2.1;
		double left_region_boundary = height * (1 - boundary);
		double right_region_boundary = height * boundary;
		boolean cont;
		double dist_max = 0.d;
		//Point start = new Point(0,0);
		//Point end = new Point(0,0);

		//List<Map<Integer, Double>> left = new ArrayList<Map<Integer, Double>>();
		//List<Map<Integer, Double>> right = new ArrayList<Map<Integer, Double>>();

		ArrayList<Double> left_scope = new ArrayList<Double>();
		ArrayList<Double> right_scope = new ArrayList<Double>();

		ArrayList<Double> left_y = new ArrayList<Double>();
		ArrayList<Double> right_y = new ArrayList<Double>();


		if (linesP.cols() > 0 && linesP.rows() > 20) {
			for (int x = 0; x < 20; x++) {

				double[] vec = linesP.get(x, 0);
				double x1 = vec[0],
						y1 = vec[1],
						x2 = vec[2],
						y2 = vec[3];

				cont = true;

				Point start = new Point(x1, y1);
				Point end = new Point(x2, y2);

				Point left1 = new Point(0, left_region_boundary);
				Point left2 = new Point(width, left_region_boundary);

				Point right1 = new Point(0, right_region_boundary);
				Point right2 = new Point(width, right_region_boundary);

				//Imgproc.line(mat, left1, left2, new Scalar(255, 255, 255), 10);
				//Imgproc.line(mat, right1, right2, new Scalar(255, 255, 0), 10);
				Imgproc.line(mat, new Point(width, -400), new Point(width / 2, height / 2), new Scalar(255, 255, 0), 10);
				Imgproc.line(mat, new Point(width, height + 400), new Point(width / 2, height / 2), new Scalar(255, 255, 0), 10);

				for (MatOfPoint n : counter) {
					if (Imgproc.pointPolygonTest(new MatOfPoint2f(n.toArray()), start, true) < 20 || Imgproc.pointPolygonTest(new MatOfPoint2f(n.toArray()), end, true) < 20) {
						cont = false;
					}
				}

				if (cont) {
					if ((Math.abs(end.x - start.x) >= Math.abs(end.y - start.y))) {
						Double slope = (y2 - y1) / (x2 - x1);
						Double y_int = y1 - slope * x1;
						if (slope < 0) {
							left_scope.add(slope);
							left_y.add(y_int);
						} else {
							right_scope.add(slope);
							right_y.add(y_int);
						}
					}
				}
			}
		}

		double right_slope_avg = average2(right_scope);
		double left_slope_avg = average2(left_scope);
		double right_y_avg = average2(right_y);
		double left_y_avg = average2(left_y);

		rightLane = make_points(height,right_slope_avg, right_y_avg);
		leftLane = make_points(height,left_slope_avg, left_y_avg);

		Imgproc.line(mat, leftLane.getStart(), leftLane.getEnd(), new Scalar(255, 0, 0, 100), 20);
		Imgproc.line(mat, rightLane.getStart(), rightLane.getEnd(), new Scalar(255, 0, 0, 100), 20);
		Point[] rook_points = new Point[4];
		rook_points[0] = rightLane.getStart();
		rook_points[1] = rightLane.getEnd();
		rook_points[2] = leftLane.getStart();
		rook_points[3] = leftLane.getEnd();
		MatOfPoint matPt = new MatOfPoint();
		matPt.fromArray(rook_points);
		List<MatOfPoint> ppt = new ArrayList<MatOfPoint>();
		ppt.add(matPt);
		//Imgproc.rectangle(mat,leftLane.getEnd(), rightLane.getStart(),new Scalar(0, 255, 0), -1, 8 ,0);
		Imgproc.fillPoly(mat, ppt, new Scalar(0, 255, 0, 100));

		return mat;
	}*/

	/*private double[] average(ArrayList<double[]> list){
		double sumX1 = 0;
		double sumX2 = 0;
		double sumY1 = 0;
		double sumY2 = 0;


		for(int a = 0; a < list.size(); a++) {
				sumX1 = sumX1 + list.get(a)[0];
				sumY1 = sumY1 + list.get(a)[1];
				sumX2 = sumX2 + list.get(a)[2];
				sumY2 = sumY2 + list.get(a)[3];
		}

		double averageX1 = sumX1 / list.size();
		double averageX2 = sumX2 / list.size();
		double averageY1 = sumY1 / list.size();
		double averageY2 = sumY2 / list.size();

		double[] average = new double[4];
		average[0]=averageX1;
		average[1]=averageY1;
		average[2]=averageX2;
		average[3]=averageY2;

		return average;
	}

	private double average2(ArrayList<Double> list){
		double sum = 0;

		for(int a = 0; a < list.size(); a++)
		{
			sum = sum + list.get(a);
		}

		double average = sum / list.size();

		return average;
	}

	private Line make_points(double width, double average_slope, double average_y){
		double y1 = width;
		double y2 = y1 * (3/5);
		//double y2 = y1 - 150;
		double x1 = (y1 - average_y) / average_slope;
		double x2 = (y2 - average_y) / average_slope;

		Line l = new Line(x1,x2,y1,y2);

		return l;
	}
*/
	private ImageAnalysis.Analyzer textAnalysis() {

		ImageAnalysis.Analyzer imageAnalysis =
				new ImageAnalysis.Analyzer() {
					@Override
					public void analyze(ImageProxy image) {
						//Analyzing live camera feed begins.

						if (textBitmap == null) {
							textBitmap = Bitmap.createBitmap(
									image.getWidth(), image.getHeight(), Bitmap.Config.ARGB_8888);
						}

						FirebaseVisionImageMetadata metadata = new FirebaseVisionImageMetadata.Builder()
								.setWidth(image.getWidth())
								.setHeight(image.getHeight())
								.setFormat(FirebaseVisionImageMetadata.IMAGE_FORMAT_NV21)
								.setRotation(FirebaseVisionImageMetadata.ROTATION_90)
								.build();

						byte [] textByteArray = Yuv2RgbConverter.yuvToByteArray(image.getImage());


						if (textByteArray != null) {

							FirebaseVisionImage fbImage = FirebaseVisionImage.fromByteArray(textByteArray, metadata);

							FirebaseVisionTextRecognizer detector = FirebaseVision.getInstance()
									.getOnDeviceTextRecognizer();

							Task<FirebaseVisionText> results = detector.processImage(fbImage)
									.addOnSuccessListener(new OnSuccessListener<FirebaseVisionText>() {
										@RequiresApi(api = Build.VERSION_CODES.N)
										@Override
										public void onSuccess(FirebaseVisionText firebaseVisionText) {
											CameraFragment.this.onSuccess(firebaseVisionText);
										}
									})
									.addOnFailureListener(
											new OnFailureListener() {
												@Override
												public void onFailure(@NonNull Exception e) {
													CameraFragment.this.onFailure(e);
												}
											});
							image.close();
						}
					}
				};
		return imageAnalysis;
	}


	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	@RequiresApi(api = Build.VERSION_CODES.N)
	protected void onSuccess(@NonNull FirebaseVisionText result) {
		for (FirebaseVisionText.TextBlock block: result.getTextBlocks()) {
			for (FirebaseVisionText.Line line: block.getLines()) {
				for (FirebaseVisionText.Element element: line.getElements()) {
					String elementText = element.getText();
					//Log.d("TEXT",elementText);
					if (Arrays.stream(values).anyMatch(elementText::equals)) {
						speed_signal.setVisibility(View.VISIBLE);
						speed_signal.setText(elementText);
					}
				}
			}
		}

	}

	protected void onFailure(@NonNull Exception e) {
		Log.w(TAG, "Text detection failed." + e);
	}



}
